package com.thebank.accounts.service;

import com.thebank.accounts.dto.AccountResponse;
import com.thebank.accounts.dto.CreateAccountRequest;
import com.thebank.accounts.entity.Account;
import com.thebank.accounts.entity.AccountStatus;
import com.thebank.accounts.repository.AccountRepository;
import com.thebank.common.exception.BusinessException;
import com.thebank.common.exception.ResourceNotFoundException;
import com.thebank.customers.entity.Customer;
import com.thebank.customers.entity.CustomerStatus;
import com.thebank.customers.repository.CustomerRepository;
import com.thebank.ledger.service.LedgerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service for account management operations.
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final IbanGenerator ibanGenerator;
    private final LedgerService ledgerService;

    public AccountService(AccountRepository accountRepository,
                          CustomerRepository customerRepository,
                          IbanGenerator ibanGenerator,
                          LedgerService ledgerService) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.ibanGenerator = ibanGenerator;
        this.ledgerService = ledgerService;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId().toString()));

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new BusinessException("CUSTOMER_NOT_ACTIVE", "Cannot create account for non-active customer");
        }

        // Generate unique IBAN
        String iban;
        do {
            iban = ibanGenerator.generateIban();
        } while (accountRepository.existsByIban(iban));

        Account account = new Account(customer, iban);
        account = accountRepository.save(account);

        return AccountResponse.from(account, BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", id.toString()));
        
        BigDecimal balance = ledgerService.getBalance(id);
        return AccountResponse.from(account, balance);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountByIban(String iban) {
        Account account = accountRepository.findByIban(iban)
                .orElseThrow(() -> new ResourceNotFoundException("Account", iban));
        
        BigDecimal balance = ledgerService.getBalance(account.getId());
        return AccountResponse.from(account, balance);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByCustomer(UUID customerId) {
        return accountRepository.findByCustomerId(customerId).stream()
                .map(account -> {
                    BigDecimal balance = ledgerService.getBalance(account.getId());
                    return AccountResponse.from(account, balance);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByUserId(UUID userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(account -> {
                    BigDecimal balance = ledgerService.getBalance(account.getId());
                    return AccountResponse.from(account, balance);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<AccountResponse> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable)
                .map(account -> {
                    BigDecimal balance = ledgerService.getBalance(account.getId());
                    return AccountResponse.from(account, balance);
                });
    }

    @Transactional
    public AccountResponse blockAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", id.toString()));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException("ACCOUNT_CLOSED", "Cannot block a closed account");
        }

        account.setStatus(AccountStatus.BLOCKED);
        account = accountRepository.save(account);
        
        BigDecimal balance = ledgerService.getBalance(id);
        return AccountResponse.from(account, balance);
    }

    @Transactional
    public AccountResponse activateAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", id.toString()));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException("ACCOUNT_CLOSED", "Cannot activate a closed account");
        }

        account.setStatus(AccountStatus.ACTIVE);
        account = accountRepository.save(account);
        
        BigDecimal balance = ledgerService.getBalance(id);
        return AccountResponse.from(account, balance);
    }

    @Transactional
    public AccountResponse closeAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", id.toString()));

        BigDecimal balance = ledgerService.getBalance(id);
        
        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException("ACCOUNT_HAS_BALANCE", 
                    "Cannot close account with non-zero balance: " + balance);
        }

        account.setStatus(AccountStatus.CLOSED);
        account = accountRepository.save(account);
        
        return AccountResponse.from(account, balance);
    }

    /**
     * Check if user owns this account.
     */
    @Transactional(readOnly = true)
    public boolean isAccountOwnedByUser(UUID accountId, UUID userId) {
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            return false;
        }
        return account.getCustomer().getUser() != null 
                && account.getCustomer().getUser().getId().equals(userId);
    }
}
