package hu.benkototh.cardgame.backend.websocket.dto;

public class RemoveClubMessageDTO {
    private long messageId;

    public RemoveClubMessageDTO() {
    }

    public RemoveClubMessageDTO(long messageId) {
        this.messageId = messageId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }
}
