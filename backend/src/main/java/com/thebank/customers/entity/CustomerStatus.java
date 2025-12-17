package com.thebank.customers.entity;

/**
 * Status of a bank customer.
 */
public enum CustomerStatus {
    ACTIVE,     // Customer can perform operations
    BLOCKED,    // Customer is blocked, no operations allowed
    PENDING     // Awaiting verification
}
