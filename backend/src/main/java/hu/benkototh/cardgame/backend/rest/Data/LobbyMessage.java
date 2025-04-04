package hu.benkototh.cardgame.backend.rest.Data;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class LobbyMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lobby_id")
    @JsonBackReference // This annotation helps with JSON serialization
    private Lobby lobby;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String content;
    private LocalDateTime sentAt;

    // Default constructor
    public LobbyMessage() {
        this.sentAt = LocalDateTime.now();
    }

    // Constructor with parameters
    public LobbyMessage(Lobby lobby, User user, String content) {
        this.lobby = lobby;
        this.user = user;
        this.content = content;
        this.sentAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Lobby getLobby() {
        return lobby;
    }

    public void setLobby(Lobby lobby) {
        this.lobby = lobby;
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

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    // Override hashCode and equals to avoid circular reference
    @Override
    public int hashCode() {
        return Objects.hash(id, user, content, sentAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LobbyMessage that = (LobbyMessage) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public String toString() {
        return "LobbyMessage{" +
                "id=" + id +
                ", user=" + (user != null ? user.getId() : null) +
                ", content='" + content + '\'' +
                ", sentAt=" + sentAt +
                '}';
    }
}