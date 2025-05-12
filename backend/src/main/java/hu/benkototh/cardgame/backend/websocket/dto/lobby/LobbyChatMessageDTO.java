package hu.benkototh.cardgame.backend.websocket.dto.lobby;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for sending a message in a lobby chat")
public class LobbyChatMessageDTO {

    @Schema(description = "ID of the user sending the message", example = "1")
    private long senderId;

    @Schema(description = "Content of the message", example = "Hello everyone in the lobby!")
    private String content;

    @Schema(description = "ID of the lobby where the message is sent", example = "42")
    private long lobbyId;

    @Schema(description = "ID of the game if the message is sent during a game", example = "g-123456", nullable = true)
    private String gameId;

    public LobbyChatMessageDTO() {
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(long lobbyId) {
        this.lobbyId = lobbyId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
}