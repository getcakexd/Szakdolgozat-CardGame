package hu.benkototh.cardgame.backend.websocket.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for transferring a user ID")
public class UserIdDTO {

    @Schema(description = "ID of the user", example = "1")
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