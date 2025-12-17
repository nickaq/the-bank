package com.thebank.ledger.service;

import com.thebank.accounts.entity.Account;
import com.thebank.accounts.repository.AccountRepository;
import com.thebank.common.exception.ResourceNotFoundException;
import com.thebank.ledger.dto.LedgerEntryResponse;
import com.thebank.ledger.dto.StatementResponse;
import com.thebank.ledger.entity.EntryDirection;
import com.thebank.ledger.entity.LedgerEntry;
import com.thebank.ledger.repository.LedgerEntryRepository;
import com.thebank.transfers.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Service for ledger operations (double-entry accounting).
 */
@Service
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final AccountRepository accountRepository;

    public LedgerService(LedgerEntryRepository ledgerEntryRepository,
                         AccountRepository accountRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Get current balance for an account.
     */
    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID accountId) {
        return ledgerEntryRepository.calculateBalance(accountId);
    }

    /**
     * Create a debit entry (money out).
     */
    @Transactional
    public LedgerEntry createDebitEntry(Account account, Transfer transfer, BigDecimal amount, String description) {
        BigDecimal currentBalance = getBalance(account.getId());
        BigDecimal newBalance = currentBalance.subtract(amount);

        LedgerEntry entry = new LedgerEntry(
                account,
                transfer,
                EntryDirection.DEBIT,
                amount,
                newBalance,
                description
        );

        return ledgerEntryRepository.save(entry);
    }

    /**
     * Create a credit entry (money in).
     */
    @Transactional
    public LedgerEntry createCreditEntry(Account account, Transfer transfer, BigDecimal amount, String description) {
        BigDecimal currentBalance = getBalance(account.getId());
        BigDecimal newBalance = currentBalance.add(amount);

        LedgerEntry entry = new LedgerEntry(
                account,
                transfer,
                EntryDirection.CREDIT,
                amount,
                newBalance,
                description
        );

        return ledgerEntryRepository.save(entry);
    }

    /**
     * Create initial funding entry for a new account (admin operation).
     */
    @Transactional
    public LedgerEntry createInitialFunding(UUID accountId, BigDecimal amount, String description) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId.toString()));

        return createCreditEntry(account, null, amount, 
                description != null ? description : "Initial funding");
    }

    /**
     * Get account statement with pagination.
     */
    @Transactional(readOnly = true)
    public StatementResponse getStatement(UUID accountId, Instant fromDate, Instant toDate, int page, int size) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId.toString()));

        Pageable pageable = PageRequest.of(page, size);
        Page<LedgerEntry> entriesPage;

        if (fromDate != null && toDate != null) {
            entriesPage = ledgerEntryRepository.findByAccountIdAndDateRange(
                    accountId, fromDate, toDate, pageable);
        } else {
            entriesPage = ledgerEntryRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable);
        }

        BigDecimal closingBalance = getBalance(accountId);

        return new StatementResponse(
                accountId,
                account.getIban(),
                BigDecimal.ZERO, // Opening balance would require complex calculation
                closingBalance,
                fromDate,
                toDate,
                entriesPage.getContent().stream().map(LedgerEntryResponse::from).toList(),
                (int) entriesPage.getTotalElements(),
                page,
                size
        );
    }

    /**
     * Get statement entries as page.
     */
    @Transactional(readOnly = true)
    public Page<LedgerEntryResponse> getStatementPage(UUID accountId, Pageable pageable) {
        return ledgerEntryRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable)
                .map(LedgerEntryResponse::from);
    }
}
