package com.thebank.ledger.repository;

import com.thebank.ledger.entity.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for LedgerEntry entity operations.
 */
@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    /**
     * Calculate balance as sum of credits minus sum of debits.
     */
    @Query("""
            SELECT COALESCE(
                SUM(CASE WHEN e.direction = 'CREDIT' THEN e.amount ELSE -e.amount END), 
                0
            )
            FROM LedgerEntry e 
            WHERE e.account.id = :accountId
            """)
    BigDecimal calculateBalance(@Param("accountId") UUID accountId);

    /**
     * Get the latest entry for an account to read current balance.
     */
    @Query("SELECT e FROM LedgerEntry e WHERE e.account.id = :accountId ORDER BY e.createdAt DESC LIMIT 1")
    Optional<LedgerEntry> findLatestByAccountId(@Param("accountId") UUID accountId);

    /**
     * Get statement (all entries) for an account with pagination.
     */
    Page<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);

    /**
     * Get statement filtered by date range.
     */
    @Query("""
            SELECT e FROM LedgerEntry e 
            WHERE e.account.id = :accountId 
            AND e.createdAt >= :fromDate 
            AND e.createdAt <= :toDate 
            ORDER BY e.createdAt DESC
            """)
    Page<LedgerEntry> findByAccountIdAndDateRange(
            @Param("accountId") UUID accountId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable
    );

    /**
     * Find entries by transfer ID.
     */
    Page<LedgerEntry> findByTransferId(UUID transferId, Pageable pageable);
}
