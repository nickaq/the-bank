package com.thebank.ledger.dto;

import com.thebank.ledger.entity.EntryDirection;
import com.thebank.ledger.entity.LedgerEntry;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for ledger entry (statement line).
 */
public record LedgerEntryResponse(
        UUID id,
        UUID accountId,
        UUID transferId,
        EntryDirection direction,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String description,
        Instant createdAt
) {
    public static LedgerEntryResponse from(LedgerEntry entry) {
        return new LedgerEntryResponse(
                entry.getId(),
                entry.getAccount().getId(),
                entry.getTransfer() != null ? entry.getTransfer().getId() : null,
                entry.getDirection(),
                entry.getAmount(),
                entry.getBalanceAfter(),
                entry.getDescription(),
                entry.getCreatedAt()
        );
    }
}
