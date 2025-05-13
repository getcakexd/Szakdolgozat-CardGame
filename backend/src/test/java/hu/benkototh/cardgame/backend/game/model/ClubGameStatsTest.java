package hu.benkototh.cardgame.backend.game.model;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class ClubGameStatsTest {

    private ClubGameStats clubGameStats;
    private Club club;
    private Game gameDefinition;

    @BeforeEach
    void setUp() {
        clubGameStats = new ClubGameStats();
        club = new Club();
        club.setId(1L);
        club.setName("Test Club");
        
        gameDefinition = new Game();
        gameDefinition.setId(1L);
        gameDefinition.setName("Test Game");
    }

    @Test
    void testInitialValues() {
        assertEquals(0, clubGameStats.getGamesPlayed());
        assertEquals(0, clubGameStats.getGamesDrawn());
        assertEquals(0, clubGameStats.getTotalPoints());
        assertEquals(0, clubGameStats.getHighestScore());
        assertEquals(0, clubGameStats.getTotalFatsCollected());
        assertEquals(0, clubGameStats.getUniquePlayersCount());
        assertNotNull(clubGameStats.getLastPlayed());
    }

    @Test
    void testSettersAndGetters() {
        Date now = new Date();
        
        clubGameStats.setId(1L);
        clubGameStats.setClub(club);
        clubGameStats.setGameDefinition(gameDefinition);
        clubGameStats.setGamesPlayed(10);
        clubGameStats.setGamesDrawn(2);
        clubGameStats.setTotalPoints(500);
        clubGameStats.setHighestScore(120);
        clubGameStats.setTotalFatsCollected(30);
        clubGameStats.setUniquePlayersCount(5);
        clubGameStats.setLastPlayed(now);
        
        assertEquals(1L, clubGameStats.getId());
        assertEquals(club, clubGameStats.getClub());
        assertEquals(gameDefinition, clubGameStats.getGameDefinition());
        assertEquals(10, clubGameStats.getGamesPlayed());
        assertEquals(2, clubGameStats.getGamesDrawn());
        assertEquals(500, clubGameStats.getTotalPoints());
        assertEquals(120, clubGameStats.getHighestScore());
        assertEquals(30, clubGameStats.getTotalFatsCollected());
        assertEquals(5, clubGameStats.getUniquePlayersCount());
        assertEquals(now, clubGameStats.getLastPlayed());
    }

    @Test
    void testIncrementGamesPlayed() {
        Date before = clubGameStats.getLastPlayed();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        clubGameStats.incrementGamesPlayed();
        assertEquals(1, clubGameStats.getGamesPlayed());
        assertTrue(clubGameStats.getLastPlayed().after(before));
        
        before = clubGameStats.getLastPlayed();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        clubGameStats.incrementGamesPlayed();
        assertEquals(2, clubGameStats.getGamesPlayed());
        assertTrue(clubGameStats.getLastPlayed().after(before));
    }

    @Test
    void testIncrementGamesDrawn() {
        Date before = clubGameStats.getLastPlayed();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        clubGameStats.incrementGamesDrawn();
        assertEquals(1, clubGameStats.getGamesDrawn());
        assertTrue(clubGameStats.getLastPlayed().after(before));
    }

    @Test
    void testAddPoints() {
        clubGameStats.addPoints(100);
        assertEquals(100, clubGameStats.getTotalPoints());
        assertEquals(100, clubGameStats.getHighestScore());
        
        clubGameStats.addPoints(50);
        assertEquals(150, clubGameStats.getTotalPoints());
        assertEquals(100, clubGameStats.getHighestScore());
        
        clubGameStats.addPoints(120);
        assertEquals(270, clubGameStats.getTotalPoints());
        assertEquals(120, clubGameStats.getHighestScore());
    }

    @Test
    void testAddFatsCollected() {
        clubGameStats.addFatsCollected(5);
        assertEquals(5, clubGameStats.getTotalFatsCollected());
        
        clubGameStats.addFatsCollected(3);
        assertEquals(8, clubGameStats.getTotalFatsCollected());
    }

    @Test
    void testIncrementUniquePlayersCount() {
        clubGameStats.incrementUniquePlayersCount();
        assertEquals(1, clubGameStats.getUniquePlayersCount());
        
        clubGameStats.incrementUniquePlayersCount();
        assertEquals(2, clubGameStats.getUniquePlayersCount());
    }
}
