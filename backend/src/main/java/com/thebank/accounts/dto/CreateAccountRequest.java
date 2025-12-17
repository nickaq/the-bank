package com.thebank.accounts.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for creating an account.
 */
public record CreateAccountRequest(
        @NotNull(message = "Customer ID is required")
        UUID customerId
) {
}
