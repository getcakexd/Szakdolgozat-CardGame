package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.AuditLog;
import hu.benkototh.cardgame.backend.rest.controller.AuditLogController;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogRestServiceTest {

    @Mock
    private AuditLogController auditLogController;

    @Mock
    private UserController userController;

    @InjectMocks
    private AuditLogRestService auditLogRestService;

    @InjectMocks
    private AuditLogRestService.AdminAuditLogRestService adminAuditLogRestService;

    private AuditLog testLog;
    private Date startDate;
    private Date endDate;

    @BeforeEach
    void setUp() {
        testLog = new AuditLog();
        testLog.setId(1L);
        testLog.setAction("LOGIN");
        testLog.setUserId(1L);
        testLog.setTimestamp(new Date());
        testLog.setDetails("User logged in from IP 192.168.1.1");

        startDate = new Date(System.currentTimeMillis() - 86400000);
        endDate = new Date();
    }

    @Test
    void testGetAllLogs() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(testLog);

        when(auditLogController.getAllLogs()).thenReturn(logs);

        ResponseEntity<List<AuditLog>> response = auditLogRestService.getAllLogs();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testLog.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testGetLogByIdSuccess() {
        when(auditLogController.getLogById(1L)).thenReturn(testLog);

        ResponseEntity<?> response = auditLogRestService.getLogById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testLog, response.getBody());
    }

    @Test
    void testGetLogByIdNotFound() {
        when(auditLogController.getLogById(1L)).thenReturn(null);

        ResponseEntity<?> response = auditLogRestService.getLogById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        assertEquals("Audit log not found", ((Map<String, String>) response.getBody()).get("message"));
    }

    @Test
    void testGetLogsByUser() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(testLog);

        when(auditLogController.getLogsByUser(1L)).thenReturn(logs);

        ResponseEntity<List<AuditLog>> response = auditLogRestService.getLogsByUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testLog.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testGetLogsByAction() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(testLog);

        when(auditLogController.getLogsByAction("LOGIN")).thenReturn(logs);

        ResponseEntity<List<AuditLog>> response = auditLogRestService.getLogsByAction("LOGIN");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testLog.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testGetLogsByDateRange() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(testLog);

        when(auditLogController.getLogsByDateRange(startDate, endDate)).thenReturn(logs);

        ResponseEntity<List<AuditLog>> response = auditLogRestService.getLogsByDateRange(startDate, endDate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testLog.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testGetAllActions() {
        List<String> actions = new ArrayList<>();
        actions.add("LOGIN");
        actions.add("LOGOUT");

        when(auditLogController.getAllActions()).thenReturn(actions);

        ResponseEntity<List<String>> response = auditLogRestService.getAllActions();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().contains("LOGIN"));
        assertTrue(response.getBody().contains("LOGOUT"));
    }

    @Test
    void testAdminGetAllLogs() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(testLog);

        when(auditLogController.getAllLogs()).thenReturn(logs);

        ResponseEntity<List<AuditLog>> response = adminAuditLogRestService.getAllLogs();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testLog.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testAdminGetFilteredLogsWithUserIdAndAction() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(testLog);

        when(auditLogController.getFilteredLogs(1L, "LOGIN")).thenReturn(logs);

        ResponseEntity<List<AuditLog>> response = adminAuditLogRestService.getFilteredLogs(1L, "LOGIN", null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testLog.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testAdminGetFilteredLogsWithUserIdActionAndDateRange() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(testLog);

        when(auditLogController.getFilteredLogs(1L, "LOGIN", startDate, endDate)).thenReturn(logs);

        ResponseEntity<List<AuditLog>> response = adminAuditLogRestService.getFilteredLogs(1L, "LOGIN", startDate, endDate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testLog.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testAdminGetFilteredLogsWithUserIdAndDateRange() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(testLog);

        when(auditLogController.getFilteredLogsByUserAndDateRange(1L, startDate, endDate)).thenReturn(logs);

        ResponseEntity<List<AuditLog>> response = adminAuditLogRestService.getFilteredLogs(1L, null, startDate, endDate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testLog.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testAdminGetFilteredLogsWithActionAndDateRange() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(testLog);

        when(auditLogController.getFilteredLogsByActionAndDateRange("LOGIN", startDate, endDate)).thenReturn(logs);

        ResponseEntity<List<AuditLog>> response = adminAuditLogRestService.getFilteredLogs(null, "LOGIN", startDate, endDate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testLog.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testAdminGetFilteredLogsWithUserId() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(testLog);

        when(auditLogController.getLogsByUser(1L)).thenReturn(logs);

        ResponseEntity<List<AuditLog>> response = adminAuditLogRestService.getFilteredLogs(1L, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testLog.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testAdminGetFilteredLogsWithAction() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(testLog);

        when(auditLogController.getLogsByAction("LOGIN")).thenReturn(logs);

        ResponseEntity<List<AuditLog>> response = adminAuditLogRestService.getFilteredLogs(null, "LOGIN", null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testLog.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testAdminGetFilteredLogsWithDateRange() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(testLog);

        when(auditLogController.getLogsByDateRange(startDate, endDate)).thenReturn(logs);

        ResponseEntity<List<AuditLog>> response = adminAuditLogRestService.getFilteredLogs(null, null, startDate, endDate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testLog.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testAdminGetFilteredLogsWithNoFilters() {
        List<AuditLog> logs = new ArrayList<>();
        logs.add(testLog);

        when(auditLogController.getAllLogs()).thenReturn(logs);

        ResponseEntity<List<AuditLog>> response = adminAuditLogRestService.getFilteredLogs(null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testLog.getId(), response.getBody().get(0).getId());
    }
}