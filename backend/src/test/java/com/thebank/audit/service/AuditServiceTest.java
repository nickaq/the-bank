package com.thebank.audit.service;

import com.thebank.audit.entity.AuditLog;
import com.thebank.audit.repository.AuditLogRepository;
import com.thebank.identity.entity.User;
import com.thebank.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuditService auditService;

    private User testUser;
    private UUID userId;
    private UUID entityId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        entityId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");
    }

    @Test
    void logAction_withUser_shouldSaveAuditLog() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAction(userId, "CREATE", "ACCOUNT", entityId, "SUCCESS", Map.of("foo", "bar"));

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog savedLog = captor.getValue();
        
        assertEquals(testUser, savedLog.getActorUser());
        assertEquals("CREATE", savedLog.getAction());
        assertEquals("ACCOUNT", savedLog.getEntityType());
        assertEquals(entityId, savedLog.getEntityId());
        assertEquals("SUCCESS", savedLog.getResult());
        assertNotNull(savedLog.getMetadata());
        assertEquals("bar", savedLog.getMetadata().get("foo"));
    }

    @Test
    void logAction_withoutUser_shouldSaveAuditLogWithNullUser() {
        // Act
        auditService.logAction(null, "LOGIN", "AUTH", null, "FAILURE", null);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog savedLog = captor.getValue();
        
        assertNull(savedLog.getActorUser());
        assertEquals("LOGIN", savedLog.getAction());
    }

    @Test
    void logSuccess_shouldLogSuccess() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        auditService.logSuccess(userId, "UPDATE", "USER", userId);

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog savedLog = captor.getValue();
        
        assertEquals("SUCCESS", savedLog.getResult());
        assertEquals("UPDATE", savedLog.getAction());
    }

    @Test
    void logFailure_shouldLogFailureWithReason() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        auditService.logFailure(userId, "DELETE", "ACCOUNT", entityId, "ACCESS_DENIED");

        // Assert
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog savedLog = captor.getValue();
        
        assertEquals("FAILURE", savedLog.getResult());
        assertNotNull(savedLog.getMetadata());
        assertEquals("ACCESS_DENIED", savedLog.getMetadata().get("reason"));
    }
    
    @Test
    void getAllLogs_shouldReturnPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        AuditLog auditLog = new AuditLog();
        Page<AuditLog> page = new PageImpl<>(Collections.singletonList(auditLog));
        when(auditLogRepository.findAll(pageable)).thenReturn(page);

        // Act
        var result = auditService.getAllLogs(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(auditLogRepository).findAll(pageable);
    }
}
