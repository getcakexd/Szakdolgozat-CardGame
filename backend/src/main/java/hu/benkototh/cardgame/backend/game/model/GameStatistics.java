package hu.benkototh.cardgame.backend.game.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "game_statistics")
public class GameStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String gameId;
    private long gameDefinitionId;
    private String gameType;
    private int score;
    private boolean won;
    private boolean drawn;
    private int fatCardsCollected;
    private int tricksTaken;
    private boolean friendly;

    @Temporal(TemporalType.TIMESTAMP)
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
