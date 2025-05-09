package hu.benkototh.cardgame.backend.game.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import hu.benkototh.cardgame.backend.rest.Data.Club;

@Entity
@Table(name = "club_stats")
@Schema(description = "Represents overall game statistics for a club")
public class ClubStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the club statistics", example = "1")
    private Long id;

    @OneToOne
    @JoinColumn(name = "club_id", unique = true,
            foreignKey = @ForeignKey(name = "fk_clubstats_club",
                    foreignKeyDefinition = "FOREIGN KEY (club_id) REFERENCES clubs(id) ON DELETE CASCADE"))
    @Schema(description = "Club these statistics belong to")
    private Club club;

    @Schema(description = "Total number of games played by club members", example = "120")
    private int gamesPlayed;

    @Schema(description = "Total number of games drawn by club members", example = "10")
    private int gamesDrawn;

    @Schema(description = "Total points earned by all club members", example = "5430")
    private int totalPoints;

    @Schema(description = "Total fat cards collected by all club members", example = "345")
    private int totalFatsCollected;

    @Schema(description = "Number of unique players who have played games in this club", example = "15")
    private int uniquePlayersCount;

    @Schema(description = "ID of the most played game in this club", example = "2")
    private int mostActiveGameId;

    public ClubStats() {
        this.gamesPlayed = 0;
        this.gamesDrawn = 0;
        this.totalPoints = 0;
        this.totalFatsCollected = 0;
        this.uniquePlayersCount = 0;
        this.mostActiveGameId = 0;
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

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
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

    public int getTotalFatsCollected() {
        return totalFatsCollected;
    }

    public void setTotalFatsCollected(int totalFatsCollected) {
        this.totalFatsCollected = totalFatsCollected;
    }

    public int getUniquePlayersCount() {
        return uniquePlayersCount;
    }

    public void setUniquePlayersCount(int uniquePlayersCount) {
        this.uniquePlayersCount = uniquePlayersCount;
    }

    public int getMostActiveGameId() {
        return mostActiveGameId;
    }

    public void setMostActiveGameId(int mostActiveGameId) {
        this.mostActiveGameId = mostActiveGameId;
    }

    @Schema(description = "Increments the games played counter by 1")
    public void incrementGamesPlayed() {
        this.gamesPlayed++;
    }

    @Schema(description = "Increments the games drawn counter by 1")
    public void incrementGamesDrawn() {
        this.gamesDrawn++;
    }

    @Schema(description = "Adds points to the total points counter")
    public void addPoints(int points) {
        this.totalPoints += points;
    }

    @Schema(description = "Adds fat cards to the total fats collected counter")
    public void addFatsCollected(int fats) {
        this.totalFatsCollected += fats;
    }

    @Schema(description = "Increments the unique players count by 1")
    public void incrementUniquePlayersCount() {
        this.uniquePlayersCount++;
    }
}