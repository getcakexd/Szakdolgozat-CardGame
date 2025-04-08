package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.AuditLog;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class AuditLogController {

    @Autowired
    private IAuditLogRepository auditLogRepository;

    public AuditLog logAction(String action, User user, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setUser(user);
        log.setDetails(details);
        log.setTimestamp(new Date());
        log.setIpAddress(getClientIpAddress());
        return auditLogRepository.save(log);
    }

    public AuditLog logAction(String action, Long userId, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setUserId(userId);
        log.setDetails(details);
        log.setTimestamp(new Date());
        log.setIpAddress(getClientIpAddress());
        return auditLogRepository.save(log);
    }

    public List<AuditLog> getLogsByUser(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }

    public List<AuditLog> getLogsByAction(String action) {
        return auditLogRepository.findByAction(action);
    }

    public List<AuditLog> getLogsByDateRange(Date startDate, Date endDate) {
        return auditLogRepository.findByTimestampBetween(startDate, endDate);
    }

    private String getClientIpAddress() {
        // In a real implementation, you would get this from the request context
        // For now, we'll return a placeholder
        return "0.0.0.0";
    }
}
