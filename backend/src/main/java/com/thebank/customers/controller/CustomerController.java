package com.thebank.customers.controller;

import com.thebank.customers.dto.CreateCustomerRequest;
import com.thebank.customers.dto.CustomerResponse;
import com.thebank.customers.dto.UpdateCustomerRequest;
import com.thebank.customers.entity.CustomerStatus;
import com.thebank.customers.service.CustomerService;
import com.thebank.identity.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for customer operations.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Customers", description = "Customer management operations")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // ==================== Admin Endpoints ====================

    @PostMapping("/admin/customers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new customer (Admin only)")
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/admin/customers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all customers with pagination (Admin only)")
    public ResponseEntity<Page<CustomerResponse>> getAllCustomers(
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        Page<CustomerResponse> customers;
        if (search != null && !search.isEmpty()) {
            customers = customerService.searchCustomers(search, pageable);
        } else if (status != null) {
            customers = customerService.getCustomersByStatus(status, pageable);
        } else {
            customers = customerService.getAllCustomers(pageable);
        }
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/admin/customers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get customer by ID (Admin only)")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.getCustomer(id));
    }

    @PutMapping("/admin/customers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update customer (Admin only)")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @PostMapping("/admin/customers/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Block a customer (Admin only)")
    public ResponseEntity<CustomerResponse> blockCustomer(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.blockCustomer(id));
    }

    @PostMapping("/admin/customers/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate a customer (Admin only)")
    public ResponseEntity<CustomerResponse> activateCustomer(@PathVariable UUID id) {
        return ResponseEntity.ok(customerService.activateCustomer(id));
    }

    // ==================== Client Endpoints ====================

    @GetMapping("/me")
    @Operation(summary = "Get current user's customer profile")
    public ResponseEntity<CustomerResponse> getMyProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(customerService.getCustomerByUserId(user.getId()));
    }
}
