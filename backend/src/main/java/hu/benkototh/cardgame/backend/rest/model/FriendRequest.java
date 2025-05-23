package hu.benkototh.cardgame.backend.rest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name = "friend_requests")
@Schema(description = "Represents a friend request from one user to another")
public class FriendRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the friend request", example = "1")
    private long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_friendrequest_sender",
                    foreignKeyDefinition = "FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE"))
    @Schema(description = "User who sent the friend request")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_friendrequest_receiver",
                    foreignKeyDefinition = "FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE"))
    @Schema(description = "User who received the friend request")
    private User receiver;

    @Schema(description = "Current status of the friend request", example = "pending",
            allowableValues = {"pending", "accepted", "rejected", "canceled"})
    private String status;

    public FriendRequest() {
        this.status = "pending";
    }

    public FriendRequest(User sender, User receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.status = "pending";
    }

    public FriendRequest(User sender, User receiver, String status) {
        this.sender = sender;
        this.receiver = receiver;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}