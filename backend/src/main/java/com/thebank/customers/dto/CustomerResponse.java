package com.thebank.customers.dto;

import com.thebank.customers.entity.Customer;
import com.thebank.customers.entity.CustomerStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for customer data.
 */
public record CustomerResponse(
        UUID id,
        String fullName,
        String email,
        String phone,
        CustomerStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
