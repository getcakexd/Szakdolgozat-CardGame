package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.AuditLog;
import hu.benkototh.cardgame.backend.rest.Data.LogRequest;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.controller.AuditLogController;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for audit log operations.
 * Only accessible by users with ROOT role.
 */
@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogRestService {

    @Autowired
    private AuditLogController auditLogController;

    @Autowired
    private UserController userController;

    @GetMapping("/all")
    public ResponseEntity<?> getAllLogs() {

        try {
            List<AuditLog> logs = auditLogController.getAllLogs();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return createErrorResponse("Error retrieving audit logs", e);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getLogsByUser(@PathVariable Long userId) {

        try {
            List<AuditLog> logs = auditLogController.getLogsByUser(userId);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return createErrorResponse("Error retrieving audit logs for user: " + userId, e);
        }
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<?> getLogsByAction(@PathVariable String action) {

        try {
            List<AuditLog> logs = auditLogController.getLogsByAction(action);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return createErrorResponse("Error retrieving audit logs for action: " + action, e);
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<?> getLogsByDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") Date endDate) {

        try {
            List<AuditLog> logs = auditLogController.getLogsByDateRange(startDate, endDate);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return createErrorResponse("Error retrieving audit logs for date range", e);
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") Date endDate) {

        try {
            List<AuditLog> logs;

            if (userId != null && action != null && startDate != null && endDate != null) {
                logs = auditLogController.getFilteredLogs(userId, action, startDate, endDate);
            } else if (userId != null && action != null) {
                logs = auditLogController.getFilteredLogs(userId, action);
            } else if (userId != null && startDate != null && endDate != null) {
                logs = auditLogController.getFilteredLogsByUserAndDateRange(userId, startDate, endDate);
            } else if (action != null && startDate != null && endDate != null) {
                logs = auditLogController.getFilteredLogsByActionAndDateRange(action, startDate, endDate);
            } else if (userId != null) {
                logs = auditLogController.getLogsByUser(userId);
            } else if (action != null) {
                logs = auditLogController.getLogsByAction(action);
            } else if (startDate != null && endDate != null) {
                logs = auditLogController.getLogsByDateRange(startDate, endDate);
            } else {
                logs = auditLogController.getAllLogs();
            }

            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return createErrorResponse("Error retrieving filtered audit logs", e);
        }
    }

    @PostMapping
    public ResponseEntity<?> createLog(@RequestBody LogRequest logRequest) {

        try {
            User user = userController.getUser(logRequest.getUserId());
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found with ID: " + logRequest.getUserId());
            }

            AuditLog log = auditLogController.logAction(logRequest.getAction(), user, logRequest.getDetails());
            return ResponseEntity.status(HttpStatus.CREATED).body(log);
        } catch (Exception e) {
            return createErrorResponse("Error creating audit log", e);
        }
    }

    private ResponseEntity<?> createErrorResponse(String message, Exception e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        response.put("details", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}