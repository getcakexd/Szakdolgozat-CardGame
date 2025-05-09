package hu.benkototh.cardgame.backend.websocket.dto.club;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for unsending (retracting) a message in a club chat")
public class UnsendClubMessageDTO {

    @Schema(description = "ID of the message to unsend", example = "123")
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