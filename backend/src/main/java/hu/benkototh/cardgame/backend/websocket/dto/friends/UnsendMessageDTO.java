package hu.benkototh.cardgame.backend.websocket.dto.friends;

public class UnsendMessageDTO {
    private long messageId;

    public UnsendMessageDTO() {
    }

    public UnsendMessageDTO(long messageId) {
        this.messageId = messageId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }
}
