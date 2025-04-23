package hu.benkototh.cardgame.backend.websocket.dto;

public class ChatMessageDTO {
    private long senderId;
    private long receiverId;
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
