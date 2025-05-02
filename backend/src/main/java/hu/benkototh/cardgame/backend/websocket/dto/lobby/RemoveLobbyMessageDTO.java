package hu.benkototh.cardgame.backend.websocket.dto.lobby;

public class RemoveLobbyMessageDTO {
    private long messageId;

    public RemoveLobbyMessageDTO() {
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }
}
