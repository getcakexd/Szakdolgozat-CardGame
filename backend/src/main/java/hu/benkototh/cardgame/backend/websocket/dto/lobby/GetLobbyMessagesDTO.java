package hu.benkototh.cardgame.backend.websocket.dto.lobby;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for requesting messages from a lobby chat")
public class GetLobbyMessagesDTO {

    @Schema(description = "ID of the lobby to get messages from", example = "42")
    private long lobbyId;

    @Schema(description = "ID of the game if requesting messages during a game", example = "g-123456", nullable = true)
    private String gameId;

    public GetLobbyMessagesDTO() {
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