package com.thebank.transfers.repository;

import com.thebank.transfers.entity.Transfer;
import com.thebank.transfers.entity.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Transfer entity operations.
 */
@Repository
public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    Page<Transfer> findByStatus(TransferStatus status, Pageable pageable);

    @Query("SELECT t FROM Transfer t WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId ORDER BY t.createdAt DESC")
    Page<Transfer> findByAccountId(@Param("accountId") UUID accountId, Pageable pageable);

    @Query("""
            SELECT t FROM Transfer t 
            WHERE (t.fromAccount.customer.user.id = :userId OR t.toAccount.customer.user.id = :userId)
            ORDER BY t.createdAt DESC
            """)
    Page<Transfer> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
            SELECT t FROM Transfer t 
            WHERE t.fromAccount.customer.user.id = :userId
            ORDER BY t.createdAt DESC
            """)
    Page<Transfer> findOutgoingByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT t FROM Transfer t WHERE t.createdAt >= :fromDate AND t.createdAt <= :toDate ORDER BY t.createdAt DESC")
    Page<Transfer> findByDateRange(
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable
    );
}
