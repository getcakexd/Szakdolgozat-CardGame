package hu.benkototh.cardgame.backend.game.model;

import jakarta.persistence.*;
import hu.benkototh.cardgame.backend.rest.Data.User;

@Entity
@Table(name = "user_stats")
public class UserStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id", unique = true,
            foreignKey = @ForeignKey(name = "fk_userstats_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"))
    private User user;

    private int gamesPlayed;
    private int gamesWon;
    private int gamesLost;
    private int gamesAbandoned;
    private int totalPoints;
    private int currentWinStreak;
    private int biggestWinStreak;
    private int totalFatsCollected;

    public UserStats() {
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.gamesLost = 0;
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

    public void incrementGamesPlayed() {
        this.gamesPlayed++;
    }

    public void incrementGamesWon() {
        this.gamesWon++;
        this.currentWinStreak++;
        if (this.currentWinStreak > this.biggestWinStreak) {
            this.biggestWinStreak = this.currentWinStreak;
        }
    }

    public void incrementGamesLost() {
        this.gamesLost++;
        this.currentWinStreak = 0;
    }

    public void incrementGamesAbandoned() {
        this.gamesAbandoned++;
        this.currentWinStreak = 0;
    }

    public void addPoints(int points) {
        this.totalPoints += points;
    }

    public void addFatsCollected(int fats) {
        this.totalFatsCollected += fats;
    }
}
