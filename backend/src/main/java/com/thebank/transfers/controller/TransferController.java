package com.thebank.transfers.controller;

import com.thebank.common.exception.BusinessException;
import com.thebank.identity.entity.User;
import com.thebank.transfers.dto.CreateTransferRequest;
import com.thebank.transfers.dto.TransferResponse;
import com.thebank.transfers.entity.TransferStatus;
import com.thebank.transfers.service.TransferService;
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
 * REST controller for transfer operations.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Transfers", description = "Internal bank transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    // ==================== Client Endpoints ====================

    @PostMapping("/transfers")
    @Operation(summary = "Create a new transfer between accounts")
    public ResponseEntity<TransferResponse> createTransfer(
            @Valid @RequestBody CreateTransferRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal User user
    ) {
        // Use header idempotency key if not provided in body
        CreateTransferRequest finalRequest = request;
        if (request.idempotencyKey() == null && idempotencyKey != null) {
            finalRequest = new CreateTransferRequest(
                    request.fromAccountId(),
                    request.toAccountId(),
                    request.amount(),
                    request.description(),
                    idempotencyKey
            );
        }

        TransferResponse response = transferService.executeTransfer(finalRequest, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/transfers")
    @Operation(summary = "Get current user's transfers")
    public ResponseEntity<Page<TransferResponse>> getMyTransfers(
            @AuthenticationPrincipal User user,
            Pageable pageable
    ) {
        return ResponseEntity.ok(transferService.getTransfersByUserId(user.getId(), pageable));
    }

    @GetMapping("/transfers/{id}")
    @Operation(summary = "Get transfer by ID (own transfers only)")
    public ResponseEntity<TransferResponse> getTransfer(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user
    ) {
        if (!transferService.canUserAccessTransfer(id, user.getId())) {
            throw new BusinessException("ACCESS_DENIED", "You don't have access to this transfer");
        }
        return ResponseEntity.ok(transferService.getTransfer(id));
    }

    // ==================== Admin Endpoints ====================

    @GetMapping("/admin/transfers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all transfers (Admin only)")
    public ResponseEntity<Page<TransferResponse>> getAllTransfers(
            @RequestParam(required = false) TransferStatus status,
            Pageable pageable
    ) {
        if (status != null) {
            return ResponseEntity.ok(transferService.getTransfersByStatus(status, pageable));
        }
        return ResponseEntity.ok(transferService.getAllTransfers(pageable));
    }

    @GetMapping("/admin/transfers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get any transfer by ID (Admin only)")
    public ResponseEntity<TransferResponse> getAdminTransfer(@PathVariable UUID id) {
        return ResponseEntity.ok(transferService.getTransfer(id));
    }

    @GetMapping("/admin/transfers/account/{accountId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get transfers by account ID (Admin only)")
    public ResponseEntity<Page<TransferResponse>> getTransfersByAccount(
            @PathVariable UUID accountId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(transferService.getTransfersByAccountId(accountId, pageable));
    }
}
