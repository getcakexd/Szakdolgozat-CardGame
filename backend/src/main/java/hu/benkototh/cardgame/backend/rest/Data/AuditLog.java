package hu.benkototh.cardgame.backend.rest.Data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "audit_logs")
@Schema(description = "Represents an audit log entry for tracking system activities")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the audit log entry", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Type of action performed", example = "LOGIN",
            allowableValues = {"LOGIN", "LOGOUT", "CREATE_USER", "UPDATE_USER", "DELETE_USER", "PASSWORD_RESET", "EMAIL_SENT"})
    private String action;

    @Column(name = "user_id")
    @Schema(description = "ID of the user who performed the action", example = "1")
    private Long userId;

    @Column(nullable = false)
    @Schema(description = "When the action occurred", example = "2023-05-20T14:30:00Z")
    private Date timestamp;

    @Column(length = 1000)
    @Schema(description = "Additional details about the action", example = "User logged in from IP 192.168.1.1")
    private String details;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}