package hu.benkototh.cardgame.backend.websocket.dto;

public class GetClubMessagesDTO {
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
