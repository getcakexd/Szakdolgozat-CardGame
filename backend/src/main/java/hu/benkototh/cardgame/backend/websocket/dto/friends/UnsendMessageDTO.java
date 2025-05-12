package hu.benkototh.cardgame.backend.websocket.dto.friends;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for unsending (retracting) a private message")
public class UnsendMessageDTO {

    @Schema(description = "ID of the message to unsend", example = "123")
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