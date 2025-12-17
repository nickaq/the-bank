package com.thebank.identity.entity;

/**
 * Roles for authorization in TheBank system.
 */
public enum Role {
    CLIENT,   // Regular bank customer
    ADMIN,    // Bank administrator
    AUDITOR   // Audit access only (read-only)
}
