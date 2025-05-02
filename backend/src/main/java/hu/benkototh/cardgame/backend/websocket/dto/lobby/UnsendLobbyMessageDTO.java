package hu.benkototh.cardgame.backend.websocket.dto.lobby;

public class UnsendLobbyMessageDTO {
    private long messageId;

    public UnsendLobbyMessageDTO() {
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }
}
