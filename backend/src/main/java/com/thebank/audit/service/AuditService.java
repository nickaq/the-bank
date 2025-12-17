package com.thebank.audit.service;

import com.thebank.audit.dto.AuditLogResponse;
import com.thebank.audit.entity.AuditLog;
import com.thebank.audit.repository.AuditLogRepository;
import com.thebank.common.exception.ResourceNotFoundException;
import com.thebank.identity.entity.User;
import com.thebank.identity.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.UUID;

/**
 * Service for audit logging.
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * Log an audit event.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(UUID userId, String action, String entityType, UUID entityId, 
                          String result, Map<String, Object> metadata) {
        try {
            User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
            
            String ipAddress = null;
            String userAgent = null;
            
            try {
                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    HttpServletRequest request = attrs.getRequest();
                    ipAddress = getClientIpAddress(request);
                    userAgent = request.getHeader("User-Agent");
                }
            } catch (Exception e) {
                // Ignore - might not be in a request context
            }

            AuditLog auditLog = new AuditLog(
                    user,
                    action,
                    entityType,
                    entityId,
                    result,
                    ipAddress,
                    userAgent,
                    metadata
            );

            auditLogRepository.save(auditLog);
            log.debug("Audit logged: {} - {} - {} - {}", action, entityType, entityId, result);
        } catch (Exception e) {
            log.error("Failed to log audit event: {}", e.getMessage(), e);
        }
    }

    /**
     * Log success action.
     */
    public void logSuccess(UUID userId, String action, String entityType, UUID entityId) {
        logAction(userId, action, entityType, entityId, "SUCCESS", null);
    }

    /**
     * Log success action with metadata.
     */
    public void logSuccess(UUID userId, String action, String entityType, UUID entityId, Map<String, Object> metadata) {
        logAction(userId, action, entityType, entityId, "SUCCESS", metadata);
    }

    /**
     * Log failure action.
     */
    public void logFailure(UUID userId, String action, String entityType, UUID entityId, String reason) {
        logAction(userId, action, entityType, entityId, "FAILURE", Map.of("reason", reason));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // Query methods

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable)
                .map(AuditLogResponse::from);
    }

    @Transactional(readOnly = true)
    public AuditLogResponse getLog(UUID id) {
        AuditLog log = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AuditLog", id.toString()));
        return AuditLogResponse.from(log);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getLogsByFilters(String action, String entityType, 
                                                     String result, UUID userId, Pageable pageable) {
        return auditLogRepository.findByFilters(action, entityType, result, userId, pageable)
                .map(AuditLogResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getLogsByEntityType(String entityType, Pageable pageable) {
        return auditLogRepository.findByEntityType(entityType, pageable)
                .map(AuditLogResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getLogsByEntity(String entityType, UUID entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable)
                .map(AuditLogResponse::from);
    }
}
