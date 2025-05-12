package hu.benkototh.cardgame.backend.websocket.dto.lobby;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for unsending (retracting) a message in a lobby chat")
public class UnsendLobbyMessageDTO {

    @Schema(description = "ID of the message to unsend", example = "123")
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