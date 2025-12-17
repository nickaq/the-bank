package com.thebank.accounts.controller;

import com.thebank.accounts.dto.AccountResponse;
import com.thebank.accounts.dto.CreateAccountRequest;
import com.thebank.accounts.service.AccountService;
import com.thebank.common.exception.BusinessException;
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

import java.util.List;
import java.util.UUID;

/**
 * REST controller for account operations.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Accounts", description = "Bank account management")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // ==================== Admin Endpoints ====================

    @PostMapping("/admin/accounts")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Open a new account for a customer (Admin only)")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/admin/accounts")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all accounts with pagination (Admin only)")
    public ResponseEntity<Page<AccountResponse>> getAllAccounts(Pageable pageable) {
        return ResponseEntity.ok(accountService.getAllAccounts(pageable));
    }

    @GetMapping("/admin/accounts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get account by ID (Admin only)")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getAccount(id));
    }

    @GetMapping("/admin/accounts/customer/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all accounts for a customer (Admin only)")
    public ResponseEntity<List<AccountResponse>> getAccountsByCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(accountService.getAccountsByCustomer(customerId));
    }

    @PostMapping("/admin/accounts/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Block an account (Admin only)")
    public ResponseEntity<AccountResponse> blockAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.blockAccount(id));
    }

    @PostMapping("/admin/accounts/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate an account (Admin only)")
    public ResponseEntity<AccountResponse> activateAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.activateAccount(id));
    }

    @PostMapping("/admin/accounts/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Close an account (Admin only)")
    public ResponseEntity<AccountResponse> closeAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.closeAccount(id));
    }

    // ==================== Client Endpoints ====================

    @GetMapping("/accounts")
    @Operation(summary = "Get current user's accounts")
    public ResponseEntity<List<AccountResponse>> getMyAccounts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(accountService.getAccountsByUserId(user.getId()));
    }

    @GetMapping("/accounts/{id}")
    @Operation(summary = "Get account by ID (own accounts only)")
    public ResponseEntity<AccountResponse> getMyAccount(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        // Check ownership
        if (!accountService.isAccountOwnedByUser(id, user.getId())) {
            throw new BusinessException("ACCESS_DENIED", "You don't have access to this account");
        }
        return ResponseEntity.ok(accountService.getAccount(id));
    }
}
