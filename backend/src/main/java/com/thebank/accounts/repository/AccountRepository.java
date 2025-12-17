package com.thebank.accounts.repository;

import com.thebank.accounts.entity.Account;
import com.thebank.accounts.entity.AccountStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Account entity operations.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByIban(String iban);

    List<Account> findByCustomerId(UUID customerId);

    Page<Account> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Account> findByStatus(AccountStatus status, Pageable pageable);

    boolean existsByIban(String iban);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdWithLock(@Param("id") UUID id);

    @Query("SELECT a FROM Account a WHERE a.customer.user.id = :userId")
    List<Account> findByUserId(@Param("userId") UUID userId);
}
