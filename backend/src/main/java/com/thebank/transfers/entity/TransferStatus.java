package com.thebank.transfers.entity;

/**
 * Status of a transfer.
 */
public enum TransferStatus {
    PENDING,    // Transfer is being processed
    COMPLETED,  // Transfer completed successfully
    REJECTED    // Transfer was rejected (insufficient funds, blocked account, etc.)
}
