package com.thebank.customers.dto;

import com.thebank.customers.entity.CustomerStatus;
import jakarta.validation.constraints.Email;

/**
 * Request DTO for updating a customer.
 */
public record UpdateCustomerRequest(
        String fullName,

        @Email(message = "Invalid email format")
        String email,

        String phone,

        CustomerStatus status
) {
}
