package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.AuditLog;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogController {

    @Autowired
    private IAuditLogRepository auditLogRepository;

    @Lazy
    @Autowired
    private UserController userController;

    public AuditLog logAction(String action, User user, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setUser(user);
        log.setDetails(details);
        log.setTimestamp(new Date());
        return auditLogRepository.save(log);
    }

    public AuditLog logAction(String action, Long userId, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setUser(userController.getUser(userId));
        log.setDetails(details);
        log.setTimestamp(new Date());
        return auditLogRepository.save(log);
    }

    public List<AuditLog> getLogsByUser(Long userId) {
        List<AuditLog> logs = new ArrayList<>(auditLogRepository.findByUserId(userId));
        Collections.reverse(logs);
        return logs;
    }

    public List<AuditLog> getLogsByAction(String action) {
        List<AuditLog> logs = new ArrayList<>(auditLogRepository.findByAction(action));
        Collections.reverse(logs);
        return logs;
    }

    public List<AuditLog> getLogsByDateRange(Date startDate, Date endDate) {
        List<AuditLog> logs = new ArrayList<>(auditLogRepository.findByTimestampBetween(startDate, endDate));
        Collections.reverse(logs);
        return logs;
    }

    public List<AuditLog> getAllLogs() {
        List<AuditLog> logs = new ArrayList<>(auditLogRepository.findAll());
        Collections.reverse(logs);
        return logs;
    }

    public List<AuditLog> getFilteredLogs(Long userId, String action) {
        List<AuditLog> logs = auditLogRepository.findByUserId(userId);
        List<AuditLog> filteredLogs = logs.stream()
                .filter(log -> log.getAction().equals(action))
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(filteredLogs);
        return filteredLogs;
    }

    public List<AuditLog> getFilteredLogs(Long userId, String action, Date startDate, Date endDate) {
        List<AuditLog> logs = auditLogRepository.findByTimestampBetween(startDate, endDate);
        List<AuditLog> filteredLogs = logs.stream()
                .filter(log -> log.getUserId().equals(userId) && log.getAction().equals(action))
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(filteredLogs);
        return filteredLogs;
    }

    public List<AuditLog> getFilteredLogsByUserAndDateRange(Long userId, Date startDate, Date endDate) {
        List<AuditLog> logs = auditLogRepository.findByTimestampBetween(startDate, endDate);
        List<AuditLog> filteredLogs = logs.stream()
                .filter(log -> log.getUserId().equals(userId))
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(filteredLogs);
        return filteredLogs;
    }

    public List<AuditLog> getFilteredLogsByActionAndDateRange(String action, Date startDate, Date endDate) {
        List<AuditLog> logs = auditLogRepository.findByTimestampBetween(startDate, endDate);
        List<AuditLog> filteredLogs = logs.stream()
                .filter(log -> log.getAction().equals(action))
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(filteredLogs);
        return filteredLogs;
    }
}