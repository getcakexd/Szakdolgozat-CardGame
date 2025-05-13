package hu.benkototh.cardgame.backend.game.model;

import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.Data.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class UserGameStatsTest {

    private UserGameStats userGameStats;
    private User user;
    private Game gameDefinition;

    @BeforeEach
    void setUp() {
        userGameStats = new UserGameStats();
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        
        gameDefinition = new Game();
        gameDefinition.setId(1L);
        gameDefinition.setName("Test Game");
    }

    @Test
    void testInitialValues() {
        assertEquals(0, userGameStats.getGamesPlayed());
        assertEquals(0, userGameStats.getGamesWon());
        assertEquals(0, userGameStats.getGamesLost());
        assertEquals(0, userGameStats.getGamesDrawn());
        assertEquals(0, userGameStats.getGamesAbandoned());
        assertEquals(0, userGameStats.getTotalPoints());
        assertEquals(0, userGameStats.getHighestScore());
        assertEquals(0, userGameStats.getTotalFatsCollected());
        assertEquals(0, userGameStats.getHighestFatsInGame());
        assertEquals(0, userGameStats.getCurrentWinStreak());
        assertEquals(0, userGameStats.getBiggestWinStreak());
        assertNotNull(userGameStats.getLastPlayed());
    }

    @Test
    void testSettersAndGetters() {
        Date now = new Date();
        
        userGameStats.setId(1L);
        userGameStats.setUser(user);
        userGameStats.setGameDefinition(gameDefinition);
        userGameStats.setGamesPlayed(10);
        userGameStats.setGamesWon(5);
        userGameStats.setGamesLost(3);
        userGameStats.setGamesDrawn(1);
        userGameStats.setGamesAbandoned(1);
        userGameStats.setTotalPoints(500);
        userGameStats.setHighestScore(120);
        userGameStats.setTotalFatsCollected(30);
        userGameStats.setHighestFatsInGame(8);
        userGameStats.setCurrentWinStreak(2);
        userGameStats.setBiggestWinStreak(3);
        userGameStats.setLastPlayed(now);
        
        assertEquals(1L, userGameStats.getId());
        assertEquals(user, userGameStats.getUser());
        assertEquals(gameDefinition, userGameStats.getGameDefinition());
        assertEquals(10, userGameStats.getGamesPlayed());
        assertEquals(5, userGameStats.getGamesWon());
        assertEquals(3, userGameStats.getGamesLost());
        assertEquals(1, userGameStats.getGamesDrawn());
        assertEquals(1, userGameStats.getGamesAbandoned());
        assertEquals(500, userGameStats.getTotalPoints());
        assertEquals(120, userGameStats.getHighestScore());
        assertEquals(30, userGameStats.getTotalFatsCollected());
        assertEquals(8, userGameStats.getHighestFatsInGame());
        assertEquals(2, userGameStats.getCurrentWinStreak());
        assertEquals(3, userGameStats.getBiggestWinStreak());
        assertEquals(now, userGameStats.getLastPlayed());
    }

    @Test
    void testIncrementGamesPlayed() {
        Date before = userGameStats.getLastPlayed();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        userGameStats.incrementGamesPlayed();
        assertEquals(1, userGameStats.getGamesPlayed());
        assertTrue(userGameStats.getLastPlayed().after(before));
    }

    @Test
    void testIncrementGamesWon() {
        Date before = userGameStats.getLastPlayed();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        userGameStats.incrementGamesWon();
        assertEquals(1, userGameStats.getGamesWon());
        assertEquals(1, userGameStats.getCurrentWinStreak());
        assertEquals(1, userGameStats.getBiggestWinStreak());
        assertTrue(userGameStats.getLastPlayed().after(before));
        
        before = userGameStats.getLastPlayed();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        userGameStats.incrementGamesWon();
        assertEquals(2, userGameStats.getGamesWon());
        assertEquals(2, userGameStats.getCurrentWinStreak());
        assertEquals(2, userGameStats.getBiggestWinStreak());
        assertTrue(userGameStats.getLastPlayed().after(before));
    }

    @Test
    void testIncrementGamesLost() {
        userGameStats.incrementGamesWon();
        userGameStats.incrementGamesWon();
        assertEquals(2, userGameStats.getCurrentWinStreak());
        
        Date before = userGameStats.getLastPlayed();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        userGameStats.incrementGamesLost();
        assertEquals(1, userGameStats.getGamesLost());
        assertEquals(0, userGameStats.getCurrentWinStreak());
        assertEquals(2, userGameStats.getBiggestWinStreak());
        assertTrue(userGameStats.getLastPlayed().after(before));
    }

    @Test
    void testIncrementGamesDrawn() {
        Date before = userGameStats.getLastPlayed();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        userGameStats.incrementGamesDrawn();
        assertEquals(1, userGameStats.getGamesDrawn());
        assertTrue(userGameStats.getLastPlayed().after(before));
    }

    @Test
    void testIncrementGamesAbandoned() {
        userGameStats.incrementGamesWon();
        assertEquals(1, userGameStats.getCurrentWinStreak());
        
        Date before = userGameStats.getLastPlayed();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        userGameStats.incrementGamesAbandoned();
        assertEquals(1, userGameStats.getGamesAbandoned());
        assertEquals(0, userGameStats.getCurrentWinStreak());
        assertTrue(userGameStats.getLastPlayed().after(before));
    }

    @Test
    void testAddPoints() {
        userGameStats.addPoints(100);
        assertEquals(100, userGameStats.getTotalPoints());
        assertEquals(100, userGameStats.getHighestScore());
        
        userGameStats.addPoints(50);
        assertEquals(150, userGameStats.getTotalPoints());
        assertEquals(100, userGameStats.getHighestScore());
        
        userGameStats.addPoints(120);
        assertEquals(270, userGameStats.getTotalPoints());
        assertEquals(120, userGameStats.getHighestScore());
    }

    @Test
    void testAddFatsCollected() {
        userGameStats.addFatsCollected(5);
        assertEquals(5, userGameStats.getTotalFatsCollected());
        assertEquals(5, userGameStats.getHighestFatsInGame());
        
        userGameStats.addFatsCollected(3);
        assertEquals(8, userGameStats.getTotalFatsCollected());
        assertEquals(5, userGameStats.getHighestFatsInGame());
        
        userGameStats.addFatsCollected(7);
        assertEquals(15, userGameStats.getTotalFatsCollected());
        assertEquals(7, userGameStats.getHighestFatsInGame());
    }

    @Test
    void testWinStreakTracking() {
        userGameStats.incrementGamesWon();
        userGameStats.incrementGamesWon();
        userGameStats.incrementGamesWon();
        assertEquals(3, userGameStats.getCurrentWinStreak());
        assertEquals(3, userGameStats.getBiggestWinStreak());
        
        userGameStats.incrementGamesLost();
        assertEquals(0, userGameStats.getCurrentWinStreak());
        assertEquals(3, userGameStats.getBiggestWinStreak());
        
        userGameStats.incrementGamesWon();
        userGameStats.incrementGamesWon();
        assertEquals(2, userGameStats.getCurrentWinStreak());
        assertEquals(3, userGameStats.getBiggestWinStreak());
        
        userGameStats.incrementGamesWon();
        userGameStats.incrementGamesWon();
        assertEquals(4, userGameStats.getCurrentWinStreak());
        assertEquals(4, userGameStats.getBiggestWinStreak());
    }
}
