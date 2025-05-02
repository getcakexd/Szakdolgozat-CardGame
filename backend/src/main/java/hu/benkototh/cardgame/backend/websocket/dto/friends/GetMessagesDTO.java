package hu.benkototh.cardgame.backend.websocket.dto.friends;

public class GetMessagesDTO {
    private long userId;
    private long friendId;

    public GetMessagesDTO() {
    }

    public GetMessagesDTO(long userId, long friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getFriendId() {
        return friendId;
    }

    public void setFriendId(long friendId) {
        this.friendId = friendId;
    }
}
