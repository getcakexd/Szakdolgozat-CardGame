package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.AuditLog;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IAuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogControllerTest {

    @Mock
    private IAuditLogRepository auditLogRepository;

    @Mock
    private UserController userController;

    @InjectMocks
    private AuditLogController auditLogController;

    private User testUser;
    private AuditLog testLog;
    private Date startDate;
    private Date endDate;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testLog = new AuditLog();
        testLog.setId(1L);
        testLog.setAction("LOGIN");
        testLog.setUserId(1L);
        testLog.setDetails("User logged in");
        testLog.setTimestamp(new Date());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        startDate = calendar.getTime();
        endDate = new Date();
    }

    @Test
    void testLogActionWithUser() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testLog);
        
        AuditLog result = auditLogController.logAction("LOGIN", testUser, "Test details");
        
        assertNotNull(result);
        assertEquals("LOGIN", testLog.getAction());
        assertEquals(testUser.getId(), testLog.getUserId());
        assertEquals("User logged in", testLog.getDetails());
        assertNotNull(testLog.getTimestamp());
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void testLogActionWithUserId() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testLog);
        
        AuditLog result = auditLogController.logAction("LOGIN", 1L, "Test details");
        
        assertNotNull(result);
        assertEquals("LOGIN", testLog.getAction());
        assertEquals(1L, testLog.getUserId());
        assertEquals("User logged in", testLog.getDetails());
        assertNotNull(testLog.getTimestamp());
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void testGetLogById() {
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(testLog));
        
        AuditLog result = auditLogController.getLogById(1L);
        
        assertNotNull(result);
        assertEquals(testLog.getId(), result.getId());
    }

    @Test
    void testGetLogByIdNotFound() {
        when(auditLogRepository.findById(1L)).thenReturn(Optional.empty());
        
        AuditLog result = auditLogController.getLogById(1L);
        
        assertNull(result);
    }

    @Test
    void testGetLogsByUser() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(testLog);
        
        when(auditLogRepository.findByUserId(1L)).thenReturn(logs);
        
        List<AuditLog> result = auditLogController.getLogsByUser(1L);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testLog.getId(), result.get(0).getId());
    }

    @Test
    void testGetLogsByAction() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(testLog);
        
        when(auditLogRepository.findByAction("LOGIN")).thenReturn(logs);
        
        List<AuditLog> result = auditLogController.getLogsByAction("LOGIN");
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testLog.getId(), result.get(0).getId());
    }

    @Test
    void testGetLogsByDateRange() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(testLog);
        
        when(auditLogRepository.findByTimestampBetween(startDate, endDate)).thenReturn(logs);
        
        List<AuditLog> result = auditLogController.getLogsByDateRange(startDate, endDate);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testLog.getId(), result.get(0).getId());
    }

    @Test
    void testGetAllLogs() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(testLog);
        
        when(auditLogRepository.findAll()).thenReturn(logs);
        
        List<AuditLog> result = auditLogController.getAllLogs();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testLog.getId(), result.get(0).getId());
    }

    @Test
    void testGetFilteredLogs() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(testLog);
        
        when(auditLogRepository.findByUserId(1L)).thenReturn(logs);
        
        List<AuditLog> result = auditLogController.getFilteredLogs(1L, "LOGIN");
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testLog.getId(), result.get(0).getId());
    }

    @Test
    void testGetFilteredLogsWithDateRange() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(testLog);
        
        when(auditLogRepository.findByTimestampBetween(startDate, endDate)).thenReturn(logs);
        
        List<AuditLog> result = auditLogController.getFilteredLogs(1L, "LOGIN", startDate, endDate);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testLog.getId(), result.get(0).getId());
    }

    @Test
    void testGetFilteredLogsByUserAndDateRange() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(testLog);
        
        when(auditLogRepository.findByTimestampBetween(startDate, endDate)).thenReturn(logs);
        
        List<AuditLog> result = auditLogController.getFilteredLogsByUserAndDateRange(1L, startDate, endDate);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testLog.getId(), result.get(0).getId());
    }

    @Test
    void testGetFilteredLogsByActionAndDateRange() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(testLog);
        
        when(auditLogRepository.findByTimestampBetween(startDate, endDate)).thenReturn(logs);
        
        List<AuditLog> result = auditLogController.getFilteredLogsByActionAndDateRange("LOGIN", startDate, endDate);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testLog.getId(), result.get(0).getId());
    }

    @Test
    void testGetAllActions() {
        List<AuditLog> logs = new ArrayList<>();
        
        AuditLog log1 = new AuditLog();
        log1.setAction("LOGIN");
        logs.add(log1);
        
        AuditLog log2 = new AuditLog();
        log2.setAction("LOGOUT");
        logs.add(log2);
        
        AuditLog log3 = new AuditLog();
        log3.setAction("LOGIN");
        logs.add(log3);
        
        when(auditLogRepository.findAll()).thenReturn(logs);
        
        List<String> result = auditLogController.getAllActions();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("LOGIN"));
        assertTrue(result.contains("LOGOUT"));
    }
}