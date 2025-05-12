package hu.benkototh.cardgame.backend.rest.data;

import hu.benkototh.cardgame.backend.rest.Data.AuditLog;
import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {

    @Test
    void testAuditLogCreation() {
        AuditLog auditLog = new AuditLog();
        assertNotNull(auditLog);
    }

    @Test
    void testGettersAndSetters() {
        AuditLog auditLog = new AuditLog();
        
        Long id = 1L;
        String action = "LOGIN";
        Long userId = 2L;
        Date timestamp = new Date();
        String details = "User logged in from IP 192.168.1.1";
        
        auditLog.setId(id);
        auditLog.setAction(action);
        auditLog.setUserId(userId);
        auditLog.setTimestamp(timestamp);
        auditLog.setDetails(details);
        
        assertEquals(id, auditLog.getId());
        assertEquals(action, auditLog.getAction());
        assertEquals(userId, auditLog.getUserId());
        assertEquals(timestamp, auditLog.getTimestamp());
        assertEquals(details, auditLog.getDetails());
    }
}