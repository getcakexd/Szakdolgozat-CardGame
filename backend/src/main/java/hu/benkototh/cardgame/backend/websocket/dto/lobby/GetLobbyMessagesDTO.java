package hu.benkototh.cardgame.backend.websocket.dto.lobby;

public class GetLobbyMessagesDTO {
    private long lobbyId;
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