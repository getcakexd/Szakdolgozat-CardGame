package hu.benkototh.cardgame.backend.game.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GameActionTest {

    private GameAction gameAction;

    @BeforeEach
    void setUp() {
        gameAction = new GameAction();
    }

    @Test
    void testInitialState() {
        assertNull(gameAction.getActionType());
        assertNotNull(gameAction.getParameters());
        assertTrue(gameAction.getParameters().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        gameAction.setActionType("PLAY_CARD");
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("cardIndex", 2);
        parameters.put("targetPlayerId", "player-123");
        
        gameAction.setParameters(parameters);
        
        assertEquals("PLAY_CARD", gameAction.getActionType());
        assertEquals(parameters, gameAction.getParameters());
        assertEquals(2, gameAction.getParameters().get("cardIndex"));
        assertEquals("player-123", gameAction.getParameters().get("targetPlayerId"));
    }

    @Test
    void testAddParameter() {
        gameAction.addParameter("cardIndex", 2);
        gameAction.addParameter("targetPlayerId", "player-123");
        
        assertEquals(2, gameAction.getParameters().get("cardIndex"));
        assertEquals("player-123", gameAction.getParameters().get("targetPlayerId"));
    }

    @Test
    void testGetCardParameterWithCardObject() {
        Card card = new Card(Suit.HEARTS, Rank.ACE);
        gameAction.addParameter("card", card);
        
        Card retrievedCard = gameAction.getCardParameter("card");
        
        assertNotNull(retrievedCard);
        assertEquals(Suit.HEARTS, retrievedCard.getSuit());
        assertEquals(Rank.ACE, retrievedCard.getRank());
    }

    @Test
    void testGetCardParameterWithMap() {
        Map<String, Object> cardMap = new HashMap<>();
        cardMap.put("suit", "HEARTS");
        cardMap.put("rank", "ACE");
        
        gameAction.addParameter("card", cardMap);
        
        Card retrievedCard = gameAction.getCardParameter("card");
        
        assertNotNull(retrievedCard);
        assertEquals(Suit.HEARTS, retrievedCard.getSuit());
        assertEquals(Rank.ACE, retrievedCard.getRank());
        
        assertTrue(gameAction.getParameters().get("card") instanceof Card);
    }

    @Test
    void testGetCardParameterWithInvalidMap() {
        Map<String, Object> invalidCardMap = new HashMap<>();
        invalidCardMap.put("suit", "INVALID_SUIT");
        invalidCardMap.put("rank", "ACE");
        
        gameAction.addParameter("card", invalidCardMap);
        
        Card retrievedCard = gameAction.getCardParameter("card");
        
        assertNull(retrievedCard);
    }

    @Test
    void testGetCardParameterWithNonCardObject() {
        gameAction.addParameter("card", "not a card");
        
        Card retrievedCard = gameAction.getCardParameter("card");
        
        assertNull(retrievedCard);
    }

    @Test
    void testGetCardParameterWithNonExistentKey() {
        Card retrievedCard = gameAction.getCardParameter("nonExistentKey");
        
        assertNull(retrievedCard);
    }

    @Test
    void testToString() {
        gameAction.setActionType("PLAY_CARD");
        gameAction.addParameter("cardIndex", 2);
        
        String toString = gameAction.toString();
        
        assertTrue(toString.contains("PLAY_CARD"));
        assertTrue(toString.contains("cardIndex"));
        assertTrue(toString.contains("2"));
    }
}
