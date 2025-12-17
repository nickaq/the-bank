package com.thebank.ledger.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for account statement.
 */
public record StatementResponse(
        UUID accountId,
        String iban,
        BigDecimal openingBalance,
        BigDecimal closingBalance,
        Instant fromDate,
        Instant toDate,
        java.util.List<LedgerEntryResponse> entries,
        int totalEntries,
        int page,
        int size
) {
}
