package hu.benkototh.cardgame.backend.rest.Data;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
public class LobbyPlayer {
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

    private boolean isLeader;
    private boolean isReady;
    private String sessionId;

    // Default constructor
    public LobbyPlayer() {
    }

    // Constructor with parameters
    public LobbyPlayer(User user, String sessionId) {
        this.user = user;
        this.sessionId = sessionId;
        this.isLeader = false;
        this.isReady = false;
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

    public boolean isLeader() {
        return isLeader;
    }

    public void setLeader(boolean isLeader) {
        this.isLeader = isLeader;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    // Override hashCode and equals to avoid circular reference
    @Override
    public int hashCode() {
        return Objects.hash(id, user, isLeader, isReady, sessionId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LobbyPlayer that = (LobbyPlayer) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public String toString() {
        return "LobbyPlayer{" +
                "id=" + id +
                ", user=" + (user != null ? user.getId() : null) +
                ", isLeader=" + isLeader +
                ", isReady=" + isReady +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}