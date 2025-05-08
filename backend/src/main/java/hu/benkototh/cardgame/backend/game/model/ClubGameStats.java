package hu.benkototh.cardgame.backend.game.model;

import jakarta.persistence.*;
import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.Game;

import java.util.Date;

@Entity
@Table(name = "club_game_stats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"club_id", "game_definition_id"}))
public class ClubGameStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "club_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_clubgamestats_club",
                    foreignKeyDefinition = "FOREIGN KEY (club_id) REFERENCES clubs(id) ON DELETE CASCADE"))
    private Club club;

    @ManyToOne
    @JoinColumn(name = "game_definition_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_clubgamestats_game",
                    foreignKeyDefinition = "FOREIGN KEY (game_definition_id) REFERENCES games(id) ON DELETE CASCADE"))
    private Game gameDefinition;

    private int gamesPlayed;
    private int gamesDrawn;
    private int totalPoints;
    private int highestScore;
    private int totalFatsCollected;
    private int uniquePlayersCount;

    @Temporal(TemporalType.TIMESTAMP)
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

    public void incrementGamesPlayed() {
        this.gamesPlayed++;
        this.lastPlayed = new Date();
    }

    public void incrementGamesDrawn() {
        this.gamesDrawn++;
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
    }

    public void incrementUniquePlayersCount() {
        this.uniquePlayersCount++;
    }
}
