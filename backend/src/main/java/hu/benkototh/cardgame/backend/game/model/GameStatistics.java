package hu.benkototh.cardgame.backend.game.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "game_statistics")
@Schema(description = "Represents statistics for a single game session")
public class GameStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the game statistics", example = "1")
    private Long id;

    @Schema(description = "ID of the user who played the game", example = "u-123456")
    private String userId;

    @Schema(description = "ID of the specific game session", example = "g-123456")
    private String gameId;

    @Schema(description = "ID of the game definition (game type)", example = "1")
    private long gameDefinitionId;

    @Schema(description = "Type of the game", example = "Poker", allowableValues = {"Poker", "Hearts", "Blackjack"})
    private String gameType;

    @Schema(description = "Score achieved by the player in this game", example = "85")
    private int score;

    @Schema(description = "Whether the player won the game", example = "true")
    private boolean won;

    @Schema(description = "Whether the game ended in a draw", example = "false")
    private boolean drawn;

    @Schema(description = "Number of fat cards collected by the player in this game", example = "7")
    private int fatCardsCollected;

    @Schema(description = "Number of tricks taken by the player in this game", example = "5")
    private int tricksTaken;

    @Schema(description = "Whether this was a friendly (non-ranked) game", example = "false")
    private boolean friendly;

    @Temporal(TemporalType.TIMESTAMP)
    @Schema(description = "When the game was played", example = "2023-05-20T14:30:00Z")
    private Date playedAt;

    public GameStatistics() {
        this.playedAt = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public long getGameDefinitionId() {
        return gameDefinitionId;
    }

    public void setGameDefinitionId(long gameDefinitionId) {
        this.gameDefinitionId = gameDefinitionId;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isWon() {
        return won;
    }

    public void setWon(boolean won) {
        this.won = won;
    }

    public boolean isDrawn() {
        return drawn;
    }

    public void setDrawn(boolean drawn) {
        this.drawn = drawn;
    }

    public int getFatCardsCollected() {
        return fatCardsCollected;
    }

    public void setFatCardsCollected(int fatCardsCollected) {
        this.fatCardsCollected = fatCardsCollected;
    }

    public int getTricksTaken() {
        return tricksTaken;
    }

    public void setTricksTaken(int tricksTaken) {
        this.tricksTaken = tricksTaken;
    }

    public Date getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(Date playedAt) {
        this.playedAt = playedAt;
    }

    public boolean isFriendly() {
        return friendly;
    }

    public void setFriendly(boolean friendly) {
        this.friendly = friendly;
    }
}