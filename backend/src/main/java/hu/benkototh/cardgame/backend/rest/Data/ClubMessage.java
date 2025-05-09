package hu.benkototh.cardgame.backend.rest.Data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "club_messages")
@Schema(description = "Represents a message in a club's chat")
public class ClubMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the club message", example = "1")
    private long id;

    @ManyToOne
    @JoinColumn(name = "club_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_clubmessage_club",
                    foreignKeyDefinition = "FOREIGN KEY (club_id) REFERENCES clubs(id) ON DELETE CASCADE"))
    @Schema(description = "Club this message belongs to")
    private Club club;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_clubmessage_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"))
    @Schema(description = "User who sent the message")
    private User sender;

    @Column(nullable = false)
    @Schema(description = "Content of the message", example = "Hello club members!")
    private String content;

    @Column(nullable = false)
    @Schema(description = "When the message was sent", example = "2023-05-20T14:30:00")
    private LocalDateTime timestamp;

    @Column(nullable = false)
    @Schema(description = "Status of the message", example = "sent",
            allowableValues = {"sent", "edited", "deleted"})
    private String status;

    public ClubMessage() {

    }

    public ClubMessage(Club club, User user, String content) {
        this.club = club;
        this.sender = user;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.status = "sent";
    }

    public ClubMessage(Club club, User user, String content, LocalDateTime timestamp) {
        this.club = club;
        this.sender = user;
        this.content = content;
        this.timestamp = timestamp;
        this.status = "sent";
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}