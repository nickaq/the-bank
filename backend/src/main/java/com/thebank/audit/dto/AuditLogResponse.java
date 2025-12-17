package com.thebank.audit.dto;

import com.thebank.audit.entity.AuditLog;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for audit log entry.
 */
public record AuditLogResponse(
        UUID id,
        UUID actorUserId,
        String actorEmail,
        String action,
        String entityType,
        UUID entityId,
        String result,
        String ipAddress,
        Map<String, Object> metadata,
        Instant createdAt
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getActorUser() != null ? log.getActorUser().getId() : null,
                log.getActorUser() != null ? log.getActorUser().getEmail() : null,
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getResult(),
                log.getIpAddress(),
                log.getMetadata(),
                log.getCreatedAt()
        );
    }
}
