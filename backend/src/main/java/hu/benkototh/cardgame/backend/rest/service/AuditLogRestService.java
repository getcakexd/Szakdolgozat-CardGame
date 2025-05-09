package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.AuditLog;
import hu.benkototh.cardgame.backend.rest.controller.AuditLogController;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Audit Logs", description = "Operations for viewing and managing audit logs")
public class AuditLogRestService {

    @Autowired
    private AuditLogController auditLogController;

    @Autowired
    private UserController userController;

    @Operation(summary = "Get all logs", description = "Retrieves all audit logs in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved logs",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuditLog.class)))
    })
    @GetMapping("/all")
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        return ResponseEntity.ok(auditLogController.getAllLogs());
    }

    @Operation(summary = "Get log by ID", description = "Retrieves a specific audit log by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved log",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuditLog.class))),
            @ApiResponse(responseCode = "404", description = "Audit log not found")
    })
    @GetMapping("/get/{id}")
    public ResponseEntity<?> getLogById(
            @Parameter(description = "ID of the audit log to retrieve", required = true) @PathVariable Long id) {
        AuditLog log = auditLogController.getLogById(id);

        if (log == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Audit log not found");
            return ResponseEntity.status(404).body(response);
        }

        return ResponseEntity.ok(log);
    }

    @Operation(summary = "Get logs by user", description = "Retrieves all audit logs for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved logs",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuditLog.class)))
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLog>> getLogsByUser(
            @Parameter(description = "ID of the user", required = true) @PathVariable Long userId) {
        return ResponseEntity.ok(auditLogController.getLogsByUser(userId));
    }

    @Operation(summary = "Get logs by action", description = "Retrieves all audit logs for a specific action type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved logs",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuditLog.class)))
    })
    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditLog>> getLogsByAction(
            @Parameter(description = "Action type to filter by", required = true) @PathVariable String action) {
        return ResponseEntity.ok(auditLogController.getLogsByAction(action));
    }

    @Operation(summary = "Get logs by date range", description = "Retrieves all audit logs within a specific date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved logs",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuditLog.class)))
    })
    @GetMapping("/date-range")
    public ResponseEntity<List<AuditLog>> getLogsByDateRange(
            @Parameter(description = "Start date (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @Parameter(description = "End date (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        return ResponseEntity.ok(auditLogController.getLogsByDateRange(startDate, endDate));
    }

    @Operation(summary = "Get all action types", description = "Retrieves a list of all action types in the audit logs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved action types",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/actions")
    public ResponseEntity<List<String>> getAllActions() {
        return ResponseEntity.ok(auditLogController.getAllActions());
    }

    @RestController
    @RequestMapping("/api/admin/audit-logs")
    @Tag(name = "Admin Audit Logs", description = "Admin operations for audit logs")
    public static class AdminAuditLogRestService {

        @Autowired
        private AuditLogController auditLogController;

        @Operation(summary = "Get all logs (admin)", description = "Admin access to retrieve all audit logs")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Successfully retrieved logs",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuditLog.class)))
        })
        @GetMapping("/all")
        public ResponseEntity<List<AuditLog>> getAllLogs() {
            return ResponseEntity.ok(auditLogController.getAllLogs());
        }

        @Operation(summary = "Filter logs", description = "Filter audit logs by various criteria")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered logs",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuditLog.class)))
        })
        @GetMapping("/filter")
        public ResponseEntity<List<AuditLog>> getFilteredLogs(
                @Parameter(description = "User ID to filter by") @RequestParam(required = false) Long userId,
                @Parameter(description = "Action type to filter by") @RequestParam(required = false) String action,
                @Parameter(description = "Start date (ISO format)")
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
                @Parameter(description = "End date (ISO format)")
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