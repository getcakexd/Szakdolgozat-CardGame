package hu.benkototh.cardgame.backend.game.model;

import jakarta.persistence.*;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.Data.Game;

import java.util.Date;

@Entity
@Table(name = "user_game_stats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "game_definition_id"}))
public class UserGameStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_usergamestats_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "game_definition_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_usergamestats_game",
                    foreignKeyDefinition = "FOREIGN KEY (game_definition_id) REFERENCES games(id) ON DELETE CASCADE"))
    private Game gameDefinition;

    private int gamesPlayed;
    private int gamesWon;
    private int gamesLost;
    private int gamesAbandoned;
    private int totalPoints;
    private int highestScore;
    private int totalFatsCollected;
    private int highestFatsInGame;
    private int currentWinStreak;
    private int biggestWinStreak;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastPlayed;

    public UserGameStats() {
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.gamesLost = 0;
        this.gamesAbandoned = 0;
        this.totalPoints = 0;
        this.highestScore = 0;
        this.totalFatsCollected = 0;
        this.highestFatsInGame = 0;
        this.currentWinStreak = 0;
        this.biggestWinStreak = 0;
        this.lastPlayed = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Game getGameDefinition() {
        return gameDefinition;
    }

    public void setGameDefinition(Game gameDefinition) {
        this.gameDefinition = gameDefinition;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    public int getGamesLost() {
        return gamesLost;
    }

    public void setGamesLost(int gamesLost) {
        this.gamesLost = gamesLost;
    }

    public int getGamesAbandoned() {
        return gamesAbandoned;
    }

    public void setGamesAbandoned(int gamesAbandoned) {
        this.gamesAbandoned = gamesAbandoned;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(int highestScore) {
        this.highestScore = highestScore;
    }

    public int getTotalFatsCollected() {
        return totalFatsCollected;
    }

    public void setTotalFatsCollected(int totalFatsCollected) {
        this.totalFatsCollected = totalFatsCollected;
    }

    public int getHighestFatsInGame() {
        return highestFatsInGame;
    }

    public void setHighestFatsInGame(int highestFatsInGame) {
        this.highestFatsInGame = highestFatsInGame;
    }

    public int getCurrentWinStreak() {
        return currentWinStreak;
    }

    public void setCurrentWinStreak(int currentWinStreak) {
        this.currentWinStreak = currentWinStreak;
    }

    public int getBiggestWinStreak() {
        return biggestWinStreak;
    }

    public void setBiggestWinStreak(int biggestWinStreak) {
        this.biggestWinStreak = biggestWinStreak;
    }

    public Date getLastPlayed() {
        return lastPlayed;
    }

    public void setLastPlayed(Date lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    public void incrementGamesPlayed() {
        this.gamesPlayed++;
        this.lastPlayed = new Date();
    }

    public void incrementGamesWon() {
        this.gamesWon++;
        this.currentWinStreak++;
        if (this.currentWinStreak > this.biggestWinStreak) {
            this.biggestWinStreak = this.currentWinStreak;
        }
        this.lastPlayed = new Date();
    }

    public void incrementGamesLost() {
        this.gamesLost++;
        this.currentWinStreak = 0;
        this.lastPlayed = new Date();
    }

    public void incrementGamesAbandoned() {
        this.gamesAbandoned++;
        this.currentWinStreak = 0;
        this.lastPlayed = new Date();
    }

    public void addPoints(int points) {
        this.totalPoints += points;
        if (points > this.highestScore) {
            this.highestScore = points;
        }
    }

    public void addFatsCollected(int fats) {
        this.totalFatsCollected += fats;
        if (fats > this.highestFatsInGame) {
            this.highestFatsInGame = fats;
        }
    }
}
