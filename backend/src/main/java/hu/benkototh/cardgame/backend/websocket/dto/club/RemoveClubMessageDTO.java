package hu.benkototh.cardgame.backend.websocket.dto.club;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for removing a message from a club chat")
public class RemoveClubMessageDTO {

    @Schema(description = "ID of the message to remove", example = "123")
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