package hu.benkototh.cardgame.backend.game.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import hu.benkototh.cardgame.backend.rest.model.Club;
import hu.benkototh.cardgame.backend.rest.model.Game;

import java.util.Date;

@Entity
@Table(name = "club_game_stats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"club_id", "game_definition_id"}))
@Schema(description = "Represents statistics for a specific game type played within a club")
public class ClubGameStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the club game statistics", example = "1")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "club_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_clubgamestats_club",
                    foreignKeyDefinition = "FOREIGN KEY (club_id) REFERENCES clubs(id) ON DELETE CASCADE"))
    @Schema(description = "Club these statistics belong to")
    private Club club;

    @ManyToOne
    @JoinColumn(name = "game_definition_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_clubgamestats_game",
                    foreignKeyDefinition = "FOREIGN KEY (game_definition_id) REFERENCES games(id) ON DELETE CASCADE"))
    @Schema(description = "Game definition these statistics are for")
    private Game gameDefinition;

    @Schema(description = "Number of games of this type played in this club", example = "42")
    private int gamesPlayed;

    @Schema(description = "Number of games of this type that ended in a draw in this club", example = "5")
    private int gamesDrawn;

    @Schema(description = "Total points earned by all players in this game type in this club", example = "1250")
    private int totalPoints;

    @Schema(description = "Highest score achieved by any player in this game type in this club", example = "120")
    private int highestScore;

    @Schema(description = "Total fat cards collected by all players in this game type in this club", example = "156")
    private int totalFatsCollected;

    @Schema(description = "Number of unique players who have played this game type in this club", example = "15")
    private int uniquePlayersCount;

    @Temporal(TemporalType.TIMESTAMP)
    @Schema(description = "When this game type was last played in this club", example = "2023-05-20T14:30:00Z")
    private Date lastPlayed;

    public ClubGameStats() {
        this.gamesPlayed = 0;
        this.gamesDrawn = 0;
        this.totalPoints = 0;
        this.highestScore = 0;
        this.totalFatsCollected = 0;
        this.uniquePlayersCount = 0;
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

    public int getUniquePlayersCount() {
        return uniquePlayersCount;
    }

    public void setUniquePlayersCount(int uniquePlayersCount) {
        this.uniquePlayersCount = uniquePlayersCount;
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

    @Schema(description = "Increments the unique players count by 1")
    public void incrementUniquePlayersCount() {
        this.uniquePlayersCount++;
    }
}