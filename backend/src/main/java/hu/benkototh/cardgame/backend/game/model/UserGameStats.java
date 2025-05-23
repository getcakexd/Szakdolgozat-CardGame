package hu.benkototh.cardgame.backend.game.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import hu.benkototh.cardgame.backend.rest.model.User;
import hu.benkototh.cardgame.backend.rest.model.Game;

import java.util.Date;

@Entity
@Table(name = "user_game_stats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "game_definition_id"}))
@Schema(description = "Represents game statistics for a user for a specific game type")
public class UserGameStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the user game statistics", example = "1")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_usergamestats_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"))
    @Schema(description = "User these statistics belong to")
    private User user;

    @ManyToOne
    @JoinColumn(name = "game_definition_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_usergamestats_game",
                    foreignKeyDefinition = "FOREIGN KEY (game_definition_id) REFERENCES games(id) ON DELETE CASCADE"))
    @Schema(description = "Game definition these statistics are for")
    private Game gameDefinition;

    @Schema(description = "Number of games played of this game type", example = "15")
    private int gamesPlayed;

    @Schema(description = "Number of games won of this game type", example = "8")
    private int gamesWon;

    @Schema(description = "Number of games lost of this game type", example = "6")
    private int gamesLost;

    @Schema(description = "Number of games drawn of this game type", example = "1")
    private int gamesDrawn;

    @Schema(description = "Number of games abandoned of this game type", example = "0")
    private int gamesAbandoned;

    @Schema(description = "Total points earned in this game type", example = "450")
    private int totalPoints;

    @Schema(description = "Highest score achieved in a single game", example = "120")
    private int highestScore;

    @Schema(description = "Total fat cards collected in this game type", example = "45")
    private int totalFatsCollected;

    @Schema(description = "Highest number of fat cards collected in a single game", example = "12")
    private int highestFatsInGame;

    @Schema(description = "Current consecutive win streak for this game type", example = "2")
    private int currentWinStreak;

    @Schema(description = "Biggest consecutive win streak achieved for this game type", example = "5")
    private int biggestWinStreak;

    @Temporal(TemporalType.TIMESTAMP)
    @Schema(description = "When the user last played this game type", example = "2023-05-20T14:30:00Z")
    private Date lastPlayed;

    public UserGameStats() {
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.gamesLost = 0;
        this.gamesDrawn = 0;
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

    public int getGamesDrawn() {
        return gamesDrawn;
    }

    public void setGamesDrawn(int gamesDrawn) {
        this.gamesDrawn = gamesDrawn;
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

    @Schema(description = "Increments the games played counter by 1 and updates last played timestamp")
    public void incrementGamesPlayed() {
        this.gamesPlayed++;
        this.lastPlayed = new Date();
    }

    @Schema(description = "Increments the games won counter by 1, updates win streaks, and updates last played timestamp")
    public void incrementGamesWon() {
        this.gamesWon++;
        this.currentWinStreak++;
        if (this.currentWinStreak > this.biggestWinStreak) {
            this.biggestWinStreak = this.currentWinStreak;
        }
        this.lastPlayed = new Date();
    }

    @Schema(description = "Increments the games lost counter by 1, resets current win streak, and updates last played timestamp")
    public void incrementGamesLost() {
        this.gamesLost++;
        this.currentWinStreak = 0;
        this.lastPlayed = new Date();
    }

    @Schema(description = "Increments the games drawn counter by 1 and updates last played timestamp")
    public void incrementGamesDrawn() {
        this.gamesDrawn++;
        this.lastPlayed = new Date();
    }

    @Schema(description = "Increments the games abandoned counter by 1, resets current win streak, and updates last played timestamp")
    public void incrementGamesAbandoned() {
        this.gamesAbandoned++;
        this.currentWinStreak = 0;
        this.lastPlayed = new Date();
    }

    @Schema(description = "Adds points to the total points counter and updates highest score if applicable")
    public void addPoints(int points) {
        this.totalPoints += points;
        if (points > this.highestScore) {
            this.highestScore = points;
        }
    }

    @Schema(description = "Adds fat cards to the total fats collected counter and updates highest fats in game if applicable")
    public void addFatsCollected(int fats) {
        this.totalFatsCollected += fats;
        if (fats > this.highestFatsInGame) {
            this.highestFatsInGame = fats;
        }
    }
}