package hu.benkototh.cardgame.backend.game.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.User;

import java.util.Date;

@Entity
@Table(name = "club_member_stats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"club_id", "user_id"}))
@Schema(description = "Represents game statistics for a member of a club")
public class ClubMemberStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the club member statistics", example = "1")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "club_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_clubmemberstats_club",
                    foreignKeyDefinition = "FOREIGN KEY (club_id) REFERENCES clubs(id) ON DELETE CASCADE"))
    @Schema(description = "Club these statistics belong to")
    private Club club;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_clubmemberstats_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"))
    @Schema(description = "User (club member) these statistics belong to")
    private User user;

    @Schema(description = "Number of games played by this member in this club", example = "25")
    private int gamesPlayed;

    @Schema(description = "Number of games won by this member in this club", example = "12")
    private int gamesWon;

    @Schema(description = "Number of games drawn by this member in this club", example = "3")
    private int gamesDrawn;

    @Schema(description = "Total points earned by this member in this club", example = "750")
    private int totalPoints;

    @Schema(description = "Highest score achieved by this member in this club", example = "95")
    private int highestScore;

    @Schema(description = "Total fat cards collected by this member in this club", example = "65")
    private int totalFatsCollected;

    @Temporal(TemporalType.TIMESTAMP)
    @Schema(description = "When this member last played in this club", example = "2023-05-20T14:30:00Z")
    private Date lastPlayed;

    public ClubMemberStats() {
        this.gamesPlayed = 0;
        this.gamesWon = 0;
        this.gamesDrawn = 0;
        this.totalPoints = 0;
        this.highestScore = 0;
        this.totalFatsCollected = 0;
        this.lastPlayed = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
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

    public int getGamesDrawn() {
        return gamesDrawn;
    }

    public void setGamesDrawn(int gamesDrawn) {
        this.gamesDrawn = gamesDrawn;
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

    @Schema(description = "Increments the games won counter by 1 and updates last played timestamp")
    public void incrementGamesWon() {
        this.gamesWon++;
        this.lastPlayed = new Date();
    }

    @Schema(description = "Increments the games drawn counter by 1 and updates last played timestamp")
    public void incrementGamesDrawn() {
        this.gamesDrawn++;
        this.lastPlayed = new Date();
    }

    @Schema(description = "Adds points to the total points counter and updates highest score if applicable")
    public void addPoints(int points) {
        this.totalPoints += points;
        if (points > this.highestScore) {
            this.highestScore = points;
        }
    }

    @Schema(description = "Adds fat cards to the total fats collected counter")
    public void addFatsCollected(int fats) {
        this.totalFatsCollected += fats;
    }
}