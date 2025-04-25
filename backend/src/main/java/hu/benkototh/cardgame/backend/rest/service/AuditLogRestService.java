package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.AuditLog;
import hu.benkototh.cardgame.backend.rest.controller.AuditLogController;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogRestService {

    @Autowired
    private AuditLogController auditLogController;

    @Autowired
    private UserController userController;

    @GetMapping("/all")
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        return ResponseEntity.ok(auditLogController.getAllLogs());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getLogById(@PathVariable Long id) {
        AuditLog log = auditLogController.getLogById(id);

        if (log == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Audit log not found");
            return ResponseEntity.status(404).body(response);
        }

        return ResponseEntity.ok(log);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLog>> getLogsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(auditLogController.getLogsByUser(userId));
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditLog>> getLogsByAction(@PathVariable String action) {
        return ResponseEntity.ok(auditLogController.getLogsByAction(action));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<AuditLog>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        return ResponseEntity.ok(auditLogController.getLogsByDateRange(startDate, endDate));
    }

    @GetMapping("/actions")
    public ResponseEntity<List<String>> getAllActions() {
        return ResponseEntity.ok(auditLogController.getAllActions());
    }

    @RestController
    @RequestMapping("/api/admin/audit-logs")
    public static class AdminAuditLogRestService {

        @Autowired
        private AuditLogController auditLogController;

        @GetMapping("/all")
        public ResponseEntity<List<AuditLog>> getAllLogs() {
            return ResponseEntity.ok(auditLogController.getAllLogs());
        }

        @GetMapping("/filter")
        public ResponseEntity<List<AuditLog>> getFilteredLogs(
                @RequestParam(required = false) Long userId,
                @RequestParam(required = false) String action,
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {

            if (userId != null && action != null && startDate != null && endDate != null) {
                return ResponseEntity.ok(auditLogController.getFilteredLogs(userId, action, startDate, endDate));
            } else if (userId != null && action != null) {
                return ResponseEntity.ok(auditLogController.getFilteredLogs(userId, action));
            } else if (userId != null && startDate != null && endDate != null) {
                return ResponseEntity.ok(auditLogController.getFilteredLogsByUserAndDateRange(userId, startDate, endDate));
            } else if (action != null && startDate != null && endDate != null) {
                return ResponseEntity.ok(auditLogController.getFilteredLogsByActionAndDateRange(action, startDate, endDate));
            } else if (userId != null) {
                return ResponseEntity.ok(auditLogController.getLogsByUser(userId));
            } else if (action != null) {
                return ResponseEntity.ok(auditLogController.getLogsByAction(action));
            } else if (startDate != null && endDate != null) {
                return ResponseEntity.ok(auditLogController.getLogsByDateRange(startDate, endDate));
            } else {
                return ResponseEntity.ok(auditLogController.getAllLogs());
            }
        }
    }
}
