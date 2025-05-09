package hu.benkototh.cardgame.backend.websocket.dto.club;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for sending a message in a club chat")
public class ClubChatMessageDTO {

    @Schema(description = "ID of the club where the message is sent", example = "42")
    private long clubId;

    @Schema(description = "ID of the user sending the message", example = "1")
    private long senderId;

    @Schema(description = "Content of the message", example = "Hello club members!")
    private String content;

    public ClubChatMessageDTO() {
    }

    public ClubChatMessageDTO(long clubId, long senderId, String content) {
        this.clubId = clubId;
        this.senderId = senderId;
        this.content = content;
    }

    public long getClubId() {
        return clubId;
    }

    public void setClubId(long clubId) {
        this.clubId = clubId;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}