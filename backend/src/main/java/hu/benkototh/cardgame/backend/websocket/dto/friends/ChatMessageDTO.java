package hu.benkototh.cardgame.backend.websocket.dto.friends;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for sending a private message between users")
public class ChatMessageDTO {

    @Schema(description = "ID of the user sending the message", example = "1")
    private long senderId;

    @Schema(description = "ID of the user receiving the message", example = "2")
    private long receiverId;

    @Schema(description = "Content of the message", example = "Hey, want to play a game?")
    private String content;

    public ChatMessageDTO() {
    }

    public ChatMessageDTO(long senderId, long receiverId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(long receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}