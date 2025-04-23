package hu.benkototh.cardgame.backend.websocket.dto;

public class UserIdDTO {
    private long userId;

    public UserIdDTO() {
    }

    public UserIdDTO(long userId) {
        this.userId = userId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
