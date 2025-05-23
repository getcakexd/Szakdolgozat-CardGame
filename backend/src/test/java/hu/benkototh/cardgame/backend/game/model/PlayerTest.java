package hu.benkototh.cardgame.backend.game.model;

import hu.benkototh.cardgame.backend.game.model.game.ZsirGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;
    private Card heartAce;
    private Card bellsKing;

    @BeforeEach
    void setUp() {
        player = new Player();
        heartAce = new Card(Suit.HEARTS, Rank.ACE);
        bellsKing = new Card(Suit.BELLS, Rank.KING);
    }

    @Test
    void testInitialState() {
        assertNotNull(player.getHand());
        assertTrue(player.getHand().isEmpty());
        assertNotNull(player.getWonCards());
        assertTrue(player.getWonCards().isEmpty());
        assertTrue(player.isActive());
        assertEquals(0, player.getScore());
        assertFalse(player.isAI());
    }

    @Test
    void testSettersAndGetters() {
        player.setId("player-123");
        player.setUsername("testplayer");
        player.setActive(false);
        player.setScore(100);
        player.setAI(true);
        
        CardGame mockGame = new ZsirGame() {
            @Override
            public Map<String, Integer> calculateScores() {
                return null;
            }
        };
        player.setGame(mockGame);
        
        assertEquals("player-123", player.getId());
        assertEquals("testplayer", player.getUsername());
        assertFalse(player.isActive());
        assertEquals(100, player.getScore());
        assertTrue(player.isAI());
        assertEquals(mockGame, player.getGame());
    }

    @Test
    void testAddCardToHand() {
        player.addCardToHand(heartAce);
        
        assertEquals(1, player.getHand().size());
        assertEquals(heartAce, player.getHand().get(0));
        
        player.addCardToHand(bellsKing);
        
        assertEquals(2, player.getHand().size());
        assertEquals(bellsKing, player.getHand().get(1));
    }

    @Test
    void testAddCardToHandWithNullHand() {
        player.setHand(null);
        player.addCardToHand(heartAce);
        
        assertNotNull(player.getHand());
        assertEquals(1, player.getHand().size());
        assertEquals(heartAce, player.getHand().get(0));
    }

    @Test
    void testRemoveCardFromHand() {
        player.addCardToHand(heartAce);
        player.addCardToHand(bellsKing);
        
        assertEquals(2, player.getHand().size());
        
        player.removeCardFromHand(heartAce);
        
        assertEquals(1, player.getHand().size());
        assertEquals(bellsKing, player.getHand().get(0));
        
        player.removeCardFromHand(new Card(Suit.BELLS, Rank.KING));
        
        assertTrue(player.getHand().isEmpty());
    }

    @Test
    void testRemoveCardFromHandWithNullHand() {
        player.setHand(null);
        
        player.removeCardFromHand(heartAce);
        
        assertNull(player.getHand());
    }

    @Test
    void testRemoveNonExistentCardFromHand() {
        player.addCardToHand(heartAce);
        
        assertEquals(1, player.getHand().size());
        
        player.removeCardFromHand(bellsKing);
        
        assertEquals(1, player.getHand().size());
        assertEquals(heartAce, player.getHand().get(0));
    }

    @Test
    void testSetHand() {
        List<Card> hand = new ArrayList<>();
        hand.add(heartAce);
        hand.add(bellsKing);
        
        player.setHand(hand);
        
        assertEquals(2, player.getHand().size());
        assertEquals(heartAce, player.getHand().get(0));
        assertEquals(bellsKing, player.getHand().get(1));
    }

    @Test
    void testSetWonCards() {
        List<Card> wonCards = new ArrayList<>();
        wonCards.add(heartAce);
        wonCards.add(bellsKing);
        
        player.setWonCards(wonCards);
        
        assertEquals(2, player.getWonCards().size());
        assertEquals(heartAce, player.getWonCards().get(0));
        assertEquals(bellsKing, player.getWonCards().get(1));
    }
}
