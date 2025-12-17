package com.thebank.transfers.service;

import com.thebank.accounts.entity.Account;
import com.thebank.accounts.entity.AccountStatus;
import com.thebank.accounts.repository.AccountRepository;
import com.thebank.common.exception.BusinessException;
import com.thebank.common.exception.ResourceNotFoundException;
import com.thebank.ledger.service.LedgerService;
import com.thebank.transfers.dto.CreateTransferRequest;
import com.thebank.transfers.dto.TransferResponse;
import com.thebank.transfers.entity.Transfer;
import com.thebank.transfers.entity.TransferStatus;
import com.thebank.transfers.repository.TransferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for transfer operations with atomic transactions and idempotency.
 */
@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final LedgerService ledgerService;

    public TransferService(TransferRepository transferRepository,
                           AccountRepository accountRepository,
                           LedgerService ledgerService) {
        this.transferRepository = transferRepository;
        this.accountRepository = accountRepository;
        this.ledgerService = ledgerService;
    }

    /**
     * Execute a transfer between accounts with idempotency and concurrency protection.
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TransferResponse executeTransfer(CreateTransferRequest request, UUID userId) {
        // Check idempotency - return existing transfer if found
        if (request.idempotencyKey() != null) {
            Optional<Transfer> existingTransfer = transferRepository.findByIdempotencyKey(request.idempotencyKey());
            if (existingTransfer.isPresent()) {
                log.info("Idempotent request detected, returning existing transfer: {}", existingTransfer.get().getId());
                return TransferResponse.from(existingTransfer.get());
            }
        }

        // Validate same account transfer
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new BusinessException("SAME_ACCOUNT", "Cannot transfer to the same account");
        }

        // Get accounts with pessimistic locking (in order to prevent deadlocks)
        UUID firstId = request.fromAccountId().compareTo(request.toAccountId()) < 0 
                ? request.fromAccountId() : request.toAccountId();
        UUID secondId = request.fromAccountId().compareTo(request.toAccountId()) < 0 
                ? request.toAccountId() : request.fromAccountId();

        Account firstAccount = accountRepository.findByIdWithLock(firstId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", firstId.toString()));
        Account secondAccount = accountRepository.findByIdWithLock(secondId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", secondId.toString()));

        Account fromAccount = request.fromAccountId().equals(firstId) ? firstAccount : secondAccount;
        Account toAccount = request.toAccountId().equals(firstId) ? firstAccount : secondAccount;

        // Validate ownership
        if (userId != null && (fromAccount.getCustomer().getUser() == null 
                || !fromAccount.getCustomer().getUser().getId().equals(userId))) {
            throw new BusinessException("ACCESS_DENIED", "You don't have access to the source account");
        }

        // Create transfer record
        Transfer transfer = new Transfer(
                fromAccount,
                toAccount,
                request.amount(),
                request.idempotencyKey(),
                request.description()
        );

        // Validate accounts
        String validationError = validateTransfer(fromAccount, toAccount, request.amount());
        if (validationError != null) {
            transfer.reject(validationError);
            transferRepository.save(transfer);
            throw new BusinessException("TRANSFER_REJECTED", validationError);
        }

        // Check balance
        BigDecimal fromBalance = ledgerService.getBalance(fromAccount.getId());
        if (fromBalance.compareTo(request.amount()) < 0) {
            transfer.reject("INSUFFICIENT_FUNDS");
            transferRepository.save(transfer);
            throw new BusinessException("INSUFFICIENT_FUNDS", 
                    "Insufficient funds. Available: " + fromBalance + ", Required: " + request.amount());
        }

        // Save transfer first
        transfer = transferRepository.save(transfer);

        // Create ledger entries (double-entry)
        String desc = request.description() != null ? request.description() : "Transfer";
        ledgerService.createDebitEntry(fromAccount, transfer, request.amount(), desc + " to " + toAccount.getIban());
        ledgerService.createCreditEntry(toAccount, transfer, request.amount(), desc + " from " + fromAccount.getIban());

        // Mark transfer as completed
        transfer.complete();
        transfer = transferRepository.save(transfer);

        log.info("Transfer completed: {} -> {}, amount: {}", 
                fromAccount.getIban(), toAccount.getIban(), request.amount());

        return TransferResponse.from(transfer);
    }

    private String validateTransfer(Account fromAccount, Account toAccount, BigDecimal amount) {
        if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
            return "SOURCE_ACCOUNT_NOT_ACTIVE";
        }
        if (toAccount.getStatus() != AccountStatus.ACTIVE) {
            return "DESTINATION_ACCOUNT_NOT_ACTIVE";
        }
        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            return "CURRENCY_MISMATCH";
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return "INVALID_AMOUNT";
        }
        return null;
    }

    @Transactional(readOnly = true)
    public TransferResponse getTransfer(UUID id) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", id.toString()));
        return TransferResponse.from(transfer);
    }

    @Transactional(readOnly = true)
    public Page<TransferResponse> getTransfersByUserId(UUID userId, Pageable pageable) {
        return transferRepository.findByUserId(userId, pageable)
                .map(TransferResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<TransferResponse> getTransfersByAccountId(UUID accountId, Pageable pageable) {
        return transferRepository.findByAccountId(accountId, pageable)
                .map(TransferResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<TransferResponse> getAllTransfers(Pageable pageable) {
        return transferRepository.findAll(pageable)
                .map(TransferResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<TransferResponse> getTransfersByStatus(TransferStatus status, Pageable pageable) {
        return transferRepository.findByStatus(status, pageable)
                .map(TransferResponse::from);
    }

    /**
     * Check if user has access to view this transfer.
     */
    @Transactional(readOnly = true)
    public boolean canUserAccessTransfer(UUID transferId, UUID userId) {
        Transfer transfer = transferRepository.findById(transferId).orElse(null);
        if (transfer == null) {
            return false;
        }
        
        UUID fromUserId = transfer.getFromAccount().getCustomer().getUser() != null 
                ? transfer.getFromAccount().getCustomer().getUser().getId() : null;
        UUID toUserId = transfer.getToAccount().getCustomer().getUser() != null 
                ? transfer.getToAccount().getCustomer().getUser().getId() : null;
        
        return userId.equals(fromUserId) || userId.equals(toUserId);
    }
}
