package hu.benkototh.cardgame.backend.websocket.dto.club;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO for requesting messages from a club chat")
public class GetClubMessagesDTO {

    @Schema(description = "ID of the club to get messages from", example = "42")
    private long clubId;

    public GetClubMessagesDTO() {
    }

    public GetClubMessagesDTO(long clubId) {
        this.clubId = clubId;
    }

    public long getClubId() {
        return clubId;
    }

    public void setClubId(long clubId) {
        this.clubId = clubId;
    }
}