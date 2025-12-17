package com.thebank.audit.controller;

import com.thebank.audit.dto.AuditLogResponse;
import com.thebank.audit.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for audit log access.
 */
@RestController
@RequestMapping("/api/v1/audit")
@PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
@Tag(name = "Audit", description = "Audit log access")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @Operation(summary = "Get all audit logs with pagination and filtering")
    public ResponseEntity<Page<AuditLogResponse>> getLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) UUID userId,
            Pageable pageable
    ) {
        if (action != null || entityType != null || result != null || userId != null) {
            return ResponseEntity.ok(auditService.getLogsByFilters(action, entityType, result, userId, pageable));
        }
        return ResponseEntity.ok(auditService.getAllLogs(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get audit log by ID")
    public ResponseEntity<AuditLogResponse> getLog(@PathVariable UUID id) {
        return ResponseEntity.ok(auditService.getLog(id));
    }

    @GetMapping("/entity/{entityType}")
    @Operation(summary = "Get audit logs by entity type")
    public ResponseEntity<Page<AuditLogResponse>> getLogsByEntityType(
            @PathVariable String entityType,
            Pageable pageable
    ) {
        return ResponseEntity.ok(auditService.getLogsByEntityType(entityType, pageable));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get audit logs for a specific entity")
    public ResponseEntity<Page<AuditLogResponse>> getLogsByEntity(
            @PathVariable String entityType,
            @PathVariable UUID entityId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(auditService.getLogsByEntity(entityType, entityId, pageable));
    }
}
