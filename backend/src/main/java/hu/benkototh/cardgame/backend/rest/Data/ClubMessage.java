package hu.benkototh.cardgame.backend.rest.Data;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "club_messages")
public class ClubMessage {
    @Id
    private long id;

    @ManyToOne
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String status;

    public ClubMessage() {

    }

    public ClubMessage(Club club, User user, String content) {
        this.club = club;
        this.user = user;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.status = "unread";
    }

    public ClubMessage(Club club, User user, String content, LocalDateTime timestamp) {
        this.club = club;
        this.user = user;
        this.content = content;
        this.timestamp = timestamp;
        this.status = "unread";
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
