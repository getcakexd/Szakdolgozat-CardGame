package hu.benkototh.cardgame.backend.game.model;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Test
    void testCardConstructor() {
        Card card = new Card(Suit.HEARTS, Rank.ACE);
        assertEquals(Suit.HEARTS, card.getSuit());
        assertEquals(Rank.ACE, card.getRank());
        assertEquals(10, card.getValue());
    }

    @Test
    void testCardValueCalculation() {
        assertEquals(10, new Card(Suit.HEARTS, Rank.ACE).getValue());
        assertEquals(10, new Card(Suit.BELLS, Rank.TEN).getValue());
        assertEquals(0, new Card(Suit.LEAVES, Rank.KING).getValue());
        assertEquals(0, new Card(Suit.ACORNS, Rank.SEVEN).getValue());
    }

    @Test
    void testSettersAndGetters() {
        Card card = new Card();
        card.setSuit(Suit.HEARTS);
        card.setRank(Rank.ACE);
        card.setValue(15);
        
        assertEquals(Suit.HEARTS, card.getSuit());
        assertEquals(Rank.ACE, card.getRank());
        assertEquals(15, card.getValue());
    }

    @Test
    void testToString() {
        Card card = new Card(Suit.HEARTS, Rank.ACE);
        assertEquals("ACE of Hearts", card.toString());
        
        card = new Card(Suit.BELLS, Rank.SEVEN);
        assertEquals("7 of Bells", card.toString());
    }

    @Test
    void testEquals() {
        Card card1 = new Card(Suit.HEARTS, Rank.ACE);
        Card card2 = new Card(Suit.HEARTS, Rank.ACE);
        Card card3 = new Card(Suit.BELLS, Rank.ACE);
        Card card4 = new Card(Suit.HEARTS, Rank.KING);
        
        assertEquals(card1, card2);
        assertNotEquals(card1, card3);
        assertNotEquals(card1, card4);
        assertNotEquals(card1, null);
        assertNotEquals(card1, "Not a card");
    }

    @Test
    void testHashCode() {
        Card card1 = new Card(Suit.HEARTS, Rank.ACE);
        Card card2 = new Card(Suit.HEARTS, Rank.ACE);
        Card card3 = new Card(Suit.BELLS, Rank.ACE);
        
        assertEquals(card1.hashCode(), card2.hashCode());
        assertNotEquals(card1.hashCode(), card3.hashCode());
    }

    @Test
    void testCreateFromStrings() {
        Card card = Card.create("HEARTS", "ACE");
        assertNotNull(card);
        assertEquals(Suit.HEARTS, card.getSuit());
        assertEquals(Rank.ACE, card.getRank());
        
        assertNull(Card.create(null, "ACE"));
        assertNull(Card.create("HEARTS", null));
        assertNull(Card.create("INVALID", "ACE"));
        assertNull(Card.create("HEARTS", "INVALID"));
    }

    @Test
    void testFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("suit", "HEARTS");
        map.put("rank", "ACE");
        
        Card card = Card.fromMap(map);
        assertNotNull(card);
        assertEquals(Suit.HEARTS, card.getSuit());
        assertEquals(Rank.ACE, card.getRank());
        
        assertNull(Card.fromMap(null));
        
        map.remove("suit");
        assertNull(Card.fromMap(map));
        
        map.put("suit", "HEARTS");
        map.remove("rank");
        assertNull(Card.fromMap(map));
        
        map.put("rank", "INVALID");
        assertNull(Card.fromMap(map));
    }
}
