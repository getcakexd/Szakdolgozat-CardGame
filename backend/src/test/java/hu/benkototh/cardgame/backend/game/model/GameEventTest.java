package hu.benkototh.cardgame.backend.game.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class GameEventTest {

    private GameEvent gameEvent;
    private final String testEventType = "CARD_PLAYED";
    private final String testGameId = "game-123";
    private final String testPlayerId = "player-456";

    @BeforeEach
    void setUp() {
        gameEvent = new GameEvent();
    }

    @Test
    void testDefaultConstructor() {
        assertNull(gameEvent.getEventType());
        assertNull(gameEvent.getGameId());
        assertNull(gameEvent.getPlayerId());
        assertNotNull(gameEvent.getTimestamp());
        assertNotNull(gameEvent.getData());
        assertTrue(gameEvent.getData().isEmpty());
    }

    @Test
    void testConstructorWithEventTypeAndGameId() {
        gameEvent = new GameEvent(testEventType, testGameId);
        
        assertEquals(testEventType, gameEvent.getEventType());
        assertEquals(testGameId, gameEvent.getGameId());
        assertNull(gameEvent.getPlayerId());
        assertNotNull(gameEvent.getTimestamp());
        assertNotNull(gameEvent.getData());
        assertTrue(gameEvent.getData().isEmpty());
    }

    @Test
    void testConstructorWithEventTypeGameIdAndPlayerId() {
        gameEvent = new GameEvent(testEventType, testGameId, testPlayerId);
        
        assertEquals(testEventType, gameEvent.getEventType());
        assertEquals(testGameId, gameEvent.getGameId());
        assertEquals(testPlayerId, gameEvent.getPlayerId());
        assertNotNull(gameEvent.getTimestamp());
        assertNotNull(gameEvent.getData());
        assertTrue(gameEvent.getData().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        gameEvent.setEventType(testEventType);
        gameEvent.setGameId(testGameId);
        gameEvent.setPlayerId(testPlayerId);
        
        assertEquals(testEventType, gameEvent.getEventType());
        assertEquals(testGameId, gameEvent.getGameId());
        assertEquals(testPlayerId, gameEvent.getPlayerId());
    }

    @Test
    void testTimestampIsCurrentTime() {
        Date before = new Date();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        gameEvent = new GameEvent();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        Date after = new Date();
        
        assertTrue(gameEvent.getTimestamp().after(before) || gameEvent.getTimestamp().equals(before));
        assertTrue(gameEvent.getTimestamp().before(after) || gameEvent.getTimestamp().equals(after));
    }

    @Test
    void testAddData() {
        gameEvent.addData("cardIndex", 2);
        gameEvent.addData("card", new Card(Suit.HEARTS, Rank.ACE));
        gameEvent.addData("message", "Player played Ace of Hearts");
        
        assertEquals(3, gameEvent.getData().size());
        assertEquals(2, gameEvent.getData().get("cardIndex"));
        assertTrue(gameEvent.getData().get("card") instanceof Card);
        assertEquals("Player played Ace of Hearts", gameEvent.getData().get("message"));
    }

    @Test
    void testGetData() {
        gameEvent.addData("cardIndex", 2);
        gameEvent.addData("message", "Player played a card");
        
        assertEquals(2, gameEvent.getData("cardIndex"));
        assertEquals("Player played a card", gameEvent.getData("message"));
        assertNull(gameEvent.getData("nonExistentKey"));
    }

    @Test
    void testDataMapModification() {
        gameEvent.addData("key1", "value1");
        
        assertEquals(1, gameEvent.getData().size());
        
        gameEvent.getData().put("key2", "value2");
        
        assertEquals(2, gameEvent.getData().size());
        assertEquals("value1", gameEvent.getData("key1"));
        assertEquals("value2", gameEvent.getData("key2"));
    }
}
