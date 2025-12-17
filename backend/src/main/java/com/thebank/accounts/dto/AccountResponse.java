package com.thebank.accounts.dto;

import com.thebank.accounts.entity.Account;
import com.thebank.accounts.entity.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for account data.
 */
public record AccountResponse(
        UUID id,
        UUID customerId,
        String customerName,
        String iban,
        String currency,
        AccountStatus status,
        BigDecimal balance,
        Instant createdAt,
        Instant updatedAt
) {
    public static AccountResponse from(Account account, BigDecimal balance) {
        return new AccountResponse(
                account.getId(),
                account.getCustomer().getId(),
                account.getCustomer().getFullName(),
                account.getIban(),
                account.getCurrency(),
                account.getStatus(),
                balance,
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    public static AccountResponse from(Account account) {
        return from(account, BigDecimal.ZERO);
    }
}
