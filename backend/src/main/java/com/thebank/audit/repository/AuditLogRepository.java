package com.thebank.audit.repository;

import com.thebank.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

/**
 * Repository for AuditLog entity operations.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByAction(String action, Pageable pageable);

    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId, Pageable pageable);

    Page<AuditLog> findByActorUserId(UUID userId, Pageable pageable);

    Page<AuditLog> findByResult(String result, Pageable pageable);

    @Query("""
            SELECT a FROM AuditLog a 
            WHERE a.createdAt >= :fromDate AND a.createdAt <= :toDate 
            ORDER BY a.createdAt DESC
            """)
    Page<AuditLog> findByDateRange(
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable
    );

    @Query("""
            SELECT a FROM AuditLog a 
            WHERE (:action IS NULL OR a.action = :action)
            AND (:entityType IS NULL OR a.entityType = :entityType)
            AND (:result IS NULL OR a.result = :result)
            AND (:userId IS NULL OR a.actorUser.id = :userId)
            ORDER BY a.createdAt DESC
            """)
    Page<AuditLog> findByFilters(
            @Param("action") String action,
            @Param("entityType") String entityType,
            @Param("result") String result,
            @Param("userId") UUID userId,
            Pageable pageable
    );
}
