package com.thebank.transfers.dto;

import com.thebank.transfers.entity.Transfer;
import com.thebank.transfers.entity.TransferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for transfer data.
 */
public record TransferResponse(
        UUID id,
        UUID fromAccountId,
        String fromIban,
        UUID toAccountId,
        String toIban,
        BigDecimal amount,
        String currency,
        TransferStatus status,
        String description,
        String failureReason,
        Instant createdAt,
        Instant completedAt
) {
    public static TransferResponse from(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getFromAccount().getId(),
                transfer.getFromAccount().getIban(),
                transfer.getToAccount().getId(),
                transfer.getToAccount().getIban(),
                transfer.getAmount(),
                transfer.getCurrency(),
                transfer.getStatus(),
                transfer.getDescription(),
                transfer.getFailureReason(),
                transfer.getCreatedAt(),
                transfer.getCompletedAt()
        );
    }
}
