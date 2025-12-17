package com.thebank.ledger.controller;

import com.thebank.accounts.service.AccountService;
import com.thebank.common.exception.BusinessException;
import com.thebank.identity.entity.User;
import com.thebank.ledger.dto.LedgerEntryResponse;
import com.thebank.ledger.dto.StatementResponse;
import com.thebank.ledger.service.LedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * REST controller for account statements.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Statements", description = "Account statement operations")
public class StatementController {

    private final LedgerService ledgerService;
    private final AccountService accountService;

    public StatementController(LedgerService ledgerService, AccountService accountService) {
        this.ledgerService = ledgerService;
        this.accountService = accountService;
    }

    // ==================== Client Endpoints ====================

    @GetMapping("/accounts/{accountId}/statement")
    @Operation(summary = "Get account statement with entries")
    public ResponseEntity<StatementResponse> getStatement(
            @PathVariable UUID accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User user
    ) {
        // Check ownership
        if (!accountService.isAccountOwnedByUser(accountId, user.getId())) {
            throw new BusinessException("ACCESS_DENIED", "You don't have access to this account");
        }

        StatementResponse statement = ledgerService.getStatement(accountId, from, to, page, size);
        return ResponseEntity.ok(statement);
    }

    @GetMapping("/accounts/{accountId}/balance")
    @Operation(summary = "Get current account balance")
    public ResponseEntity<BigDecimal> getBalance(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal User user
    ) {
        // Check ownership
        if (!accountService.isAccountOwnedByUser(accountId, user.getId())) {
            throw new BusinessException("ACCESS_DENIED", "You don't have access to this account");
        }

        return ResponseEntity.ok(ledgerService.getBalance(accountId));
    }

    // ==================== Admin Endpoints ====================

    @GetMapping("/admin/accounts/{accountId}/statement")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get any account statement (Admin only)")
    public ResponseEntity<StatementResponse> getAdminStatement(
            @PathVariable UUID accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        StatementResponse statement = ledgerService.getStatement(accountId, from, to, page, size);
        return ResponseEntity.ok(statement);
    }

    @PostMapping("/admin/accounts/{accountId}/fund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add initial funding to an account (Admin only)")
    public ResponseEntity<LedgerEntryResponse> fundAccount(
            @PathVariable UUID accountId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description
    ) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "Amount must be positive");
        }

        var entry = ledgerService.createInitialFunding(accountId, amount, description);
        return ResponseEntity.ok(LedgerEntryResponse.from(entry));
    }
}
