package hu.benkototh.cardgame.backend.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "ticket_messages")
@Schema(description = "Represents a message in a support ticket conversation")
public class TicketMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the message", example = "1")
    private long id;

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_ticketmessage_ticket",
                    foreignKeyDefinition = "FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE"))
    @JsonIgnore
    @Schema(description = "Ticket this message belongs to")
    private Ticket ticket;

    @ManyToOne
    @JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "fk_ticketmessage_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL"))
    @JsonIgnore
    @Schema(description = "User who sent the message (if authenticated)")
    private User user;

    @Schema(description = "Name of the sender", example = "John Doe")
    private String senderName;

    @Schema(description = "Email of the sender", example = "john.doe@example.com")
    private String senderEmail;

    @Schema(description = "Type of sender", example = "USER", allowableValues = {"USER", "AGENT", "ADMIN", "SYSTEM"})
    private String senderType;

    @Column(length = 1000)
    @Schema(description = "Content of the message", example = "I've tried restarting the application but still having issues...")
    private String message;

    @Schema(description = "Whether the message was sent by a support agent", example = "false")
    private boolean isFromAgent;

    @Temporal(TemporalType.TIMESTAMP)
    @Schema(description = "When the message was created", example = "2023-05-20T15:45:00Z")
    private Date createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = new Date();
        }
    }

    public TicketMessage() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isFromAgent() {
        return isFromAgent;
    }

    public void setFromAgent(boolean fromAgent) {
        isFromAgent = fromAgent;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Schema(description = "ID of the ticket this message belongs to", example = "1")
    public void setTicketId(Long ticketId) {
        if (this.ticket != null) {
            this.ticket.setId(ticketId);
        }
    }

    @Schema(description = "ID of the user who sent the message", example = "1")
    public void setUserId(Long userId) {
        if (this.user != null) {
            this.user.setId(userId);
        }
    }
}