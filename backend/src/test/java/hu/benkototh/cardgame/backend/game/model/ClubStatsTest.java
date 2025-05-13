package hu.benkototh.cardgame.backend.game.model;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClubStatsTest {

    private ClubStats clubStats;
    private Club club;

    @BeforeEach
    void setUp() {
        clubStats = new ClubStats();
        club = new Club();
        club.setId(1L);
        club.setName("Test Club");
    }

    @Test
    void testInitialValues() {
        assertEquals(0, clubStats.getGamesPlayed());
        assertEquals(0, clubStats.getGamesDrawn());
        assertEquals(0, clubStats.getTotalPoints());
        assertEquals(0, clubStats.getTotalFatsCollected());
        assertEquals(0, clubStats.getUniquePlayersCount());
        assertEquals(0, clubStats.getMostActiveGameId());
    }

    @Test
    void testSettersAndGetters() {
        clubStats.setId(1L);
        clubStats.setClub(club);
        clubStats.setGamesPlayed(10);
        clubStats.setGamesDrawn(2);
        clubStats.setTotalPoints(500);
        clubStats.setTotalFatsCollected(30);
        clubStats.setUniquePlayersCount(5);
        clubStats.setMostActiveGameId(2);
        
        assertEquals(1L, clubStats.getId());
        assertEquals(club, clubStats.getClub());
        assertEquals(10, clubStats.getGamesPlayed());
        assertEquals(2, clubStats.getGamesDrawn());
        assertEquals(500, clubStats.getTotalPoints());
        assertEquals(30, clubStats.getTotalFatsCollected());
        assertEquals(5, clubStats.getUniquePlayersCount());
        assertEquals(2, clubStats.getMostActiveGameId());
    }

    @Test
    void testIncrementGamesPlayed() {
        clubStats.incrementGamesPlayed();
        assertEquals(1, clubStats.getGamesPlayed());
        
        clubStats.incrementGamesPlayed();
        assertEquals(2, clubStats.getGamesPlayed());
    }

    @Test
    void testIncrementGamesDrawn() {
        clubStats.incrementGamesDrawn();
        assertEquals(1, clubStats.getGamesDrawn());
        
        clubStats.incrementGamesDrawn();
        assertEquals(2, clubStats.getGamesDrawn());
    }

    @Test
    void testAddPoints() {
        clubStats.addPoints(100);
        assertEquals(100, clubStats.getTotalPoints());
        
        clubStats.addPoints(50);
        assertEquals(150, clubStats.getTotalPoints());
    }

    @Test
    void testAddFatsCollected() {
        clubStats.addFatsCollected(5);
        assertEquals(5, clubStats.getTotalFatsCollected());
        
        clubStats.addFatsCollected(3);
        assertEquals(8, clubStats.getTotalFatsCollected());
    }

    @Test
    void testIncrementUniquePlayersCount() {
        clubStats.incrementUniquePlayersCount();
        assertEquals(1, clubStats.getUniquePlayersCount());
        
        clubStats.incrementUniquePlayersCount();
        assertEquals(2, clubStats.getUniquePlayersCount());
    }
}
