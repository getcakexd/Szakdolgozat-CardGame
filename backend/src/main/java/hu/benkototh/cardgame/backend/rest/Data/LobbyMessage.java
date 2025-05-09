package hu.benkototh.cardgame.backend.rest.Data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.util.Date;

@Entity
@Schema(description = "Represents a chat message in a lobby or game")
public class LobbyMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the message", example = "1")
    private long id;

    @Schema(description = "Content of the message", example = "Hello everyone!")
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_lobbymessage_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"))
    @Schema(description = "User who sent the message")
    private User user;

    @Schema(description = "ID of the game if this is a game message", example = "g-123456")
    private String gameId;

    @Schema(description = "ID of the lobby if this is a lobby message", example = "1")
    private long lobbyId;

    @Schema(description = "Whether this message is for a lobby (true) or a game (false)", example = "true")
    private boolean isLobbyMessage;

    @Temporal(TemporalType.TIMESTAMP)
    @Schema(description = "When the message was sent", example = "2023-05-20T14:35:00Z")
    private Date timestamp;

    @Schema(description = "Status of the message", example = "active", allowableValues = {"active", "deleted", "unsent"})
    private String status = "active";

    public LobbyMessage() {
        this.timestamp = new Date();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public long getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(long lobbyId) {
        this.lobbyId = lobbyId;
    }

    public boolean isLobbyMessage() {
        return isLobbyMessage;
    }

    public void setLobbyMessage(boolean lobbyMessage) {
        isLobbyMessage = lobbyMessage;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}