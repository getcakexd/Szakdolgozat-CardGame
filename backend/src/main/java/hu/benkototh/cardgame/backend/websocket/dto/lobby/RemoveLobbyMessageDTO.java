package hu.benkototh.cardgame.backend.websocket.dto.lobby;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for removing a message from a lobby chat")
public class RemoveLobbyMessageDTO {

    @Schema(description = "ID of the message to remove", example = "123")
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