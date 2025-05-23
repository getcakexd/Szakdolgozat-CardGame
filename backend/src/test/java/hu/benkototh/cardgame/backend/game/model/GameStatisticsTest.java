package hu.benkototh.cardgame.backend.game.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class GameStatisticsTest {

    private GameStatistics gameStatistics;

    @BeforeEach
    void setUp() {
        gameStatistics = new GameStatistics();
    }

    @Test
    void testInitialValues() {
        assertNull(gameStatistics.getId());
        assertNull(gameStatistics.getUserId());
        assertNull(gameStatistics.getGameId());
        assertEquals(0, gameStatistics.getGameDefinitionId());
        assertNull(gameStatistics.getGameType());
        assertEquals(0, gameStatistics.getScore());
        assertFalse(gameStatistics.isWon());
        assertFalse(gameStatistics.isDrawn());
        assertEquals(0, gameStatistics.getFatCardsCollected());
        assertEquals(0, gameStatistics.getTricksTaken());
        assertFalse(gameStatistics.isFriendly());
        assertNotNull(gameStatistics.getPlayedAt());
    }

    @Test
    void testSettersAndGetters() {
        Date now = new Date();
        
        gameStatistics.setId(1L);
        gameStatistics.setUserId("user-123");
        gameStatistics.setGameId("game-456");
        gameStatistics.setGameDefinitionId(2L);
        gameStatistics.setGameType("Poker");
        gameStatistics.setScore(85);
        gameStatistics.setWon(true);
        gameStatistics.setDrawn(false);
        gameStatistics.setFatCardsCollected(7);
        gameStatistics.setTricksTaken(5);
        gameStatistics.setFriendly(true);
        gameStatistics.setPlayedAt(now);
        
        assertEquals(1L, gameStatistics.getId());
        assertEquals("user-123", gameStatistics.getUserId());
        assertEquals("game-456", gameStatistics.getGameId());
        assertEquals(2L, gameStatistics.getGameDefinitionId());
        assertEquals("Poker", gameStatistics.getGameType());
        assertEquals(85, gameStatistics.getScore());
        assertTrue(gameStatistics.isWon());
        assertFalse(gameStatistics.isDrawn());
        assertEquals(7, gameStatistics.getFatCardsCollected());
        assertEquals(5, gameStatistics.getTricksTaken());
        assertTrue(gameStatistics.isFriendly());
        assertEquals(now, gameStatistics.getPlayedAt());
    }

    @Test
    void testWinDrawMutualExclusivity() {
        gameStatistics.setWon(true);
        gameStatistics.setDrawn(true);
        
        assertTrue(gameStatistics.isWon());
        assertTrue(gameStatistics.isDrawn());
    }
}
