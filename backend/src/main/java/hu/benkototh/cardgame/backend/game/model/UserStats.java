package hu.benkototh.cardgame.backend.game.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import hu.benkototh.cardgame.backend.rest.Data.User;

@Entity
@Table(name = "user_stats")
@Schema(description = "Represents overall game statistics for a user across all games")
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the user statistics", example = "1")
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true,
            foreignKey = @ForeignKey(name = "fk_userstats_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"))
    @Schema(description = "User these statistics belong to")
    private User user;

    @Schema(description = "Total number of games played", example = "42")
    private int gamesPlayed;

    @Schema(description = "Total number of games won", example = "25")
    private int gamesWon;

    @Schema(description = "Total number of games lost", example = "15")
    private int gamesLost;

    @Schema(description = "Total number of games drawn", example = "2")
    private int gamesDrawn;

    @Schema(description = "Total number of games abandoned", example = "0")
    private int gamesAbandoned;

    @Schema(description = "Total points earned across all games", example = "1250")
    private int totalPoints;

    @Schema(description = "Current consecutive win streak", example = "3")
    private int currentWinStreak;

    @Schema(description = "Biggest consecutive win streak achieved", example = "7")
    private int biggestWinStreak;

    @Schema(description = "Total fat cards collected across all games", example = "156")
    private int totalFatsCollected;

    public UserStats() {
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.gamesLost = 0;
        this.gamesDrawn = 0;
        this.gamesAbandoned = 0;
        this.totalPoints = 0;
        this.currentWinStreak = 0;
        this.biggestWinStreak = 0;
        this.totalFatsCollected = 0;
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

    public int getTotalFatsCollected() {
        return totalFatsCollected;
    }

    public void setTotalFatsCollected(int totalFatsCollected) {
        this.totalFatsCollected = totalFatsCollected;
    }

    @Schema(description = "Increments the games played counter by 1")
    public void incrementGamesPlayed() {
        this.gamesPlayed++;
    }

    @Schema(description = "Increments the games won counter by 1 and updates win streaks")
    public void incrementGamesWon() {
        this.gamesWon++;
        this.currentWinStreak++;
        if (this.currentWinStreak > this.biggestWinStreak) {
            this.biggestWinStreak = this.currentWinStreak;
        }
    }

    @Schema(description = "Increments the games lost counter by 1 and resets current win streak")
    public void incrementGamesLost() {
        this.gamesLost++;
        this.currentWinStreak = 0;
    }

    @Schema(description = "Increments the games drawn counter by 1")
    public void incrementGamesDrawn() {
        this.gamesDrawn++;
    }

    @Schema(description = "Increments the games abandoned counter by 1 and resets current win streak")
    public void incrementGamesAbandoned() {
        this.gamesAbandoned++;
        this.currentWinStreak = 0;
    }

    @Schema(description = "Adds points to the total points counter")
    public void addPoints(int points) {
        this.totalPoints += points;
    }

    @Schema(description = "Adds fat cards to the total fats collected counter")
    public void addFatsCollected(int fats) {
        this.totalFatsCollected += fats;
    }
}