package com.thebank.ledger.controller;

import com.thebank.accounts.service.AccountService;
import com.thebank.common.exception.BusinessException;
import com.thebank.identity.entity.User;
import com.thebank.ledger.dto.StatementResponse;
import com.thebank.ledger.service.LedgerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatementControllerTest {

    @Mock
    private LedgerService ledgerService;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private StatementController statementController;

    private User testUser;
    private UUID userId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
    }

    @Test
    void getStatement_whenOwner_shouldReturnStatement() {
        // Arrange
        when(accountService.isAccountOwnedByUser(accountId, userId)).thenReturn(true);
        StatementResponse mockResponse = new StatementResponse(
                accountId, "IBAN123", BigDecimal.ZERO, BigDecimal.TEN,
                Instant.now(), Instant.now(), Collections.emptyList(), 0, 0, 20
        );
        when(ledgerService.getStatement(eq(accountId), any(), any(), eq(0), eq(20)))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<StatementResponse> response = statementController.getStatement(
                accountId, null, null, 0, 20, testUser
        );

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("IBAN123", response.getBody().iban());
        verify(accountService).isAccountOwnedByUser(accountId, userId);
        verify(ledgerService).getStatement(eq(accountId), any(), any(), eq(0), eq(20));
    }

    @Test
    void getStatement_whenNotOwner_shouldThrowException() {
        // Arrange
        when(accountService.isAccountOwnedByUser(accountId, userId)).thenReturn(false);

        // Act & Assert
        assertThrows(BusinessException.class, () -> 
            statementController.getStatement(accountId, null, null, 0, 20, testUser)
        );
        verify(ledgerService, never()).getStatement(any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void getBalance_whenOwner_shouldReturnBalance() {
        // Arrange
        when(accountService.isAccountOwnedByUser(accountId, userId)).thenReturn(true);
        when(ledgerService.getBalance(accountId)).thenReturn(BigDecimal.valueOf(100.50));

        // Act
        ResponseEntity<BigDecimal> response = statementController.getBalance(accountId, testUser);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(BigDecimal.valueOf(100.50), response.getBody());
    }
}
