package hu.benkototh.cardgame.backend.game.model;

import jakarta.persistence.*;
import hu.benkototh.cardgame.backend.rest.Data.Club;

@Entity
@Table(name = "club_stats")
public class ClubStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "club_id", unique = true,
            foreignKey = @ForeignKey(name = "fk_clubstats_club",
                    foreignKeyDefinition = "FOREIGN KEY (club_id) REFERENCES clubs(id) ON DELETE CASCADE"))
    private Club club;

    private int gamesPlayed;
    private int totalPoints;
    private int totalFatsCollected;
    private int uniquePlayersCount;
    private int mostActiveGameId;

    public ClubStats() {
        this.gamesPlayed = 0;
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

    public void incrementGamesPlayed() {
        this.gamesPlayed++;
    }

    public void addPoints(int points) {
        this.totalPoints += points;
    }

    public void addFatsCollected(int fats) {
        this.totalFatsCollected += fats;
    }

    public void incrementUniquePlayersCount() {
        this.uniquePlayersCount++;
    }
}
