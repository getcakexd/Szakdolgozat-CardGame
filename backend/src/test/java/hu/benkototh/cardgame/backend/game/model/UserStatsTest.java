package hu.benkototh.cardgame.backend.game.model;

import hu.benkototh.cardgame.backend.rest.Data.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserStatsTest {

    private UserStats userStats;
    private User user;

    @BeforeEach
    void setUp() {
        userStats = new UserStats();
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
    }

    @Test
    void testInitialValues() {
        assertEquals(0, userStats.getGamesPlayed());
        assertEquals(0, userStats.getGamesWon());
        assertEquals(0, userStats.getGamesLost());
        assertEquals(0, userStats.getGamesDrawn());
        assertEquals(0, userStats.getGamesAbandoned());
        assertEquals(0, userStats.getTotalPoints());
        assertEquals(0, userStats.getCurrentWinStreak());
        assertEquals(0, userStats.getBiggestWinStreak());
        assertEquals(0, userStats.getTotalFatsCollected());
    }

    @Test
    void testSettersAndGetters() {
        userStats.setId(1L);
        userStats.setUser(user);
        userStats.setGamesPlayed(10);
        userStats.setGamesWon(5);
        userStats.setGamesLost(3);
        userStats.setGamesDrawn(1);
        userStats.setGamesAbandoned(1);
        userStats.setTotalPoints(500);
        userStats.setCurrentWinStreak(2);
        userStats.setBiggestWinStreak(3);
        userStats.setTotalFatsCollected(30);
        
        assertEquals(1L, userStats.getId());
        assertEquals(user, userStats.getUser());
        assertEquals(10, userStats.getGamesPlayed());
        assertEquals(5, userStats.getGamesWon());
        assertEquals(3, userStats.getGamesLost());
        assertEquals(1, userStats.getGamesDrawn());
        assertEquals(1, userStats.getGamesAbandoned());
        assertEquals(500, userStats.getTotalPoints());
        assertEquals(2, userStats.getCurrentWinStreak());
        assertEquals(3, userStats.getBiggestWinStreak());
        assertEquals(30, userStats.getTotalFatsCollected());
    }

    @Test
    void testIncrementGamesPlayed() {
        userStats.incrementGamesPlayed();
        assertEquals(1, userStats.getGamesPlayed());
        
        userStats.incrementGamesPlayed();
        assertEquals(2, userStats.getGamesPlayed());
    }

    @Test
    void testIncrementGamesWon() {
        userStats.incrementGamesWon();
        assertEquals(1, userStats.getGamesWon());
        assertEquals(1, userStats.getCurrentWinStreak());
        assertEquals(1, userStats.getBiggestWinStreak());
        
        userStats.incrementGamesWon();
        assertEquals(2, userStats.getGamesWon());
        assertEquals(2, userStats.getCurrentWinStreak());
        assertEquals(2, userStats.getBiggestWinStreak());
    }

    @Test
    void testIncrementGamesLost() {
        userStats.incrementGamesWon();
        userStats.incrementGamesWon();
        assertEquals(2, userStats.getCurrentWinStreak());
        
        userStats.incrementGamesLost();
        assertEquals(1, userStats.getGamesLost());
        assertEquals(0, userStats.getCurrentWinStreak());
        assertEquals(2, userStats.getBiggestWinStreak());
    }

    @Test
    void testIncrementGamesDrawn() {
        userStats.incrementGamesDrawn();
        assertEquals(1, userStats.getGamesDrawn());
        
        userStats.incrementGamesDrawn();
        assertEquals(2, userStats.getGamesDrawn());
    }

    @Test
    void testIncrementGamesAbandoned() {
        userStats.incrementGamesWon();
        assertEquals(1, userStats.getCurrentWinStreak());
        
        userStats.incrementGamesAbandoned();
        assertEquals(1, userStats.getGamesAbandoned());
        assertEquals(0, userStats.getCurrentWinStreak());
    }

    @Test
    void testAddPoints() {
        userStats.addPoints(100);
        assertEquals(100, userStats.getTotalPoints());
        
        userStats.addPoints(50);
        assertEquals(150, userStats.getTotalPoints());
    }

    @Test
    void testAddFatsCollected() {
        userStats.addFatsCollected(5);
        assertEquals(5, userStats.getTotalFatsCollected());
        
        userStats.addFatsCollected(3);
        assertEquals(8, userStats.getTotalFatsCollected());
    }

    @Test
    void testWinStreakTracking() {
        userStats.incrementGamesWon();
        userStats.incrementGamesWon();
        userStats.incrementGamesWon();
        assertEquals(3, userStats.getCurrentWinStreak());
        assertEquals(3, userStats.getBiggestWinStreak());
        
        userStats.incrementGamesLost();
        assertEquals(0, userStats.getCurrentWinStreak());
        assertEquals(3, userStats.getBiggestWinStreak());
        
        userStats.incrementGamesWon();
        userStats.incrementGamesWon();
        assertEquals(2, userStats.getCurrentWinStreak());
        assertEquals(3, userStats.getBiggestWinStreak());
        
        userStats.incrementGamesWon();
        userStats.incrementGamesWon();
        assertEquals(4, userStats.getCurrentWinStreak());
        assertEquals(4, userStats.getBiggestWinStreak());
    }
}
