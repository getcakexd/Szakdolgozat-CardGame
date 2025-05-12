package hu.benkototh.cardgame.backend.rest.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Schema(description = "Represents a support ticket")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the ticket", example = "1")
    private long id;

    @Column(unique = true, nullable = false)
    @Schema(description = "Unique reference code for the ticket", example = "ABC12345")
    private String reference;

    @Schema(description = "Name of the person who created the ticket", example = "John Doe")
    private String name;

    @Schema(description = "Email address of the person who created the ticket", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Subject of the ticket", example = "Problem with game connection")
    private String subject;

    @Schema(description = "Category of the ticket", example = "Technical Support",
            allowableValues = {"Technical Support", "Account Issues", "Billing", "Game Bug", "Feature Request", "Other"})
    private String category;

    @Column(length = 1000)
    @Schema(description = "Initial message content of the ticket", example = "I'm having trouble connecting to the game server...")
    private String message;

    @Schema(description = "Current status of the ticket", example = "Open",
            allowableValues = {"Open", "In Progress", "Waiting for Customer", "Resolved", "Closed"})
    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    @Schema(description = "When the ticket was created", example = "2023-05-20T14:30:00Z")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Schema(description = "When the ticket was last updated", example = "2023-05-21T10:15:00Z")
    private Date updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "fk_ticket_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL"))
    @JsonIgnore
    @Schema(description = "User who created the ticket (if authenticated)")
    private User user;

    @ManyToOne
    @JoinColumn(name = "assigned_to",
            foreignKey = @ForeignKey(name = "fk_ticket_assigned_to",
                    foreignKeyDefinition = "FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL"))
    @JsonIgnore
    @Schema(description = "Support agent assigned to the ticket")
    private User assignedTo;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    @Schema(description = "Messages associated with this ticket")
    private List<TicketMessage> messages;

    @PrePersist
    public void onCreate() {
        if (reference == null) {
            reference = generateReference();
        }
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = new Date();
    }

    private String generateReference() {
        String uuid = UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
        return uuid.substring(0, 8);
    }

    public Ticket() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public List<TicketMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<TicketMessage> messages) {
        this.messages = messages;
    }

    @Schema(description = "ID of the user who created the ticket", example = "1")
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    public void setUserId(Long userId) {
        if (user != null) {
            user.setId(userId);
        }
    }

    public void setAssignedToId(Long agentId) {
        if (assignedTo != null) {
            assignedTo.setId(agentId);
        }
    }
}