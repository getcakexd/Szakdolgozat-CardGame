package hu.benkototh.cardgame.backend.rest.Data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "user_history")
@Schema(description = "Represents a historical record of changes to a user account")
public class UserHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the history record", example = "1")
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "fk_userhistory_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"))
    @Schema(description = "User associated with this history record")
    private User user;

    @Schema(description = "Previous username before change", example = "old_username")
    private String previousUsername;

    @Schema(description = "Previous email before change", example = "old.email@example.com")
    private String previousEmail;

    @Schema(description = "When the change occurred", example = "2023-05-20T14:30:00Z")
    private Date changedAt;

    @Schema(description = "Who made the change (user ID or 'system')", example = "1")
    private String changedBy;

    public UserHistory() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPreviousUsername() {
        return previousUsername;
    }

    public void setPreviousUsername(String previousUsername) {
        this.previousUsername = previousUsername;
    }

    public String getPreviousEmail() {
        return previousEmail;
    }

    public void setPreviousEmail(String previousEmail) {
        this.previousEmail = previousEmail;
    }

    public Date getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Date changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }
}