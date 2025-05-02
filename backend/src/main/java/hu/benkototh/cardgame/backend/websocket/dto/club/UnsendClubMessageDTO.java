package hu.benkototh.cardgame.backend.websocket.dto.club;

public class UnsendClubMessageDTO {
    private long messageId;

    public UnsendClubMessageDTO() {
    }

    public UnsendClubMessageDTO(long messageId) {
        this.messageId = messageId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }
}
