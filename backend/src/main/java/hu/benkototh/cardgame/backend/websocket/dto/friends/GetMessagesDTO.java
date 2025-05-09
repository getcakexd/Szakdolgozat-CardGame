package hu.benkototh.cardgame.backend.websocket.dto.friends;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for requesting private messages between two users")
public class GetMessagesDTO {

    @Schema(description = "ID of the requesting user", example = "1")
    private long userId;

    @Schema(description = "ID of the friend whose conversation is requested", example = "2")
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