package hu.benkototh.cardgame.backend.rest.Data;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "contact_requests")
public class ContactRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String email;
    private String subject;
    private String problemType;

    @Column(length = 1000)
    private String message;

    private String status;
    private Date createdAt;

    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    public ContactRequest() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getProblemType() {
        return problemType;
    }

    public void setProblemType(String problemType) {
        this.problemType = problemType;
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

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }
}