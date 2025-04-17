package hu.benkototh.cardgame.backend.rest.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, nullable = false)
    private String reference;

    private String name;
    private String email;
    private String subject;
    private String category;

    @Column(length = 1000)
    private String message;

    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "assigned_to")
    @JsonIgnore
    private User assignedTo;

    @Column(name = "assigned_to", insertable = false, updatable = false)
    private Long assignedToId;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<TicketMessage> messages;

    @PrePersist
    protected void onCreate() {
        if (reference == null) {
            reference = generateReference();
        }
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
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

    public Long getUserId() {
        return userId != null ? userId : (user != null ? user.getId() : null);
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Long getAssignedToId() {
        return assignedToId != null ? assignedToId : (assignedTo != null ? assignedTo.getId() : null);
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    public List<TicketMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<TicketMessage> messages) {
        this.messages = messages;
    }
}