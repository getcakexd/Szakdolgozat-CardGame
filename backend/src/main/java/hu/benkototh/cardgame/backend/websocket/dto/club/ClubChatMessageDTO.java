package hu.benkototh.cardgame.backend.websocket.dto.club;

public class ClubChatMessageDTO {
    private long clubId;
    private long senderId;
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
