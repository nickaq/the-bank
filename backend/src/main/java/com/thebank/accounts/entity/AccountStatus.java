package com.thebank.accounts.entity;

/**
 * Status of a bank account.
 */
public enum AccountStatus {
    ACTIVE,     // Account is open and can receive/send transfers
    BLOCKED,    // Account is blocked, no operations allowed
    CLOSED      // Account is closed permanently
}
