package com.thebank.identity.dto;

/**
 * Response DTO for authentication operations.
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String role
) {
}
