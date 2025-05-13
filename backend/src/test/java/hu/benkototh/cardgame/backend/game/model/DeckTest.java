package hu.benkototh.cardgame.backend.game.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    @Test
    void testEmptyConstructor() {
        Deck deck = new Deck();
        assertTrue(deck.isEmpty());
        assertEquals(0, deck.size());
    }

    @Test
    void testConstructorWithCards() {
        List<Card> cards = new ArrayList<>();
        cards.add(new Card(Suit.HEARTS, Rank.ACE));
        cards.add(new Card(Suit.BELLS, Rank.KING));
        
        Deck deck = new Deck(cards);
        assertFalse(deck.isEmpty());
        assertEquals(2, deck.size());
    }

    @Test
    void testConstructorWithNullCards() {
        Deck deck = new Deck(null);
        assertTrue(deck.isEmpty());
        assertEquals(0, deck.size());
    }

    @Test
    void testInitializeHungarianDeck() {
        Deck deck = new Deck();
        deck.initializeHungarianDeck();
        
        assertEquals(32, deck.size());
        
        boolean hasHeartAce = false;
        boolean hasBellsSeven = false;
        
        for (Card card : deck.getCards()) {
            if (card.getSuit() == Suit.HEARTS && card.getRank() == Rank.ACE) {
                hasHeartAce = true;
            }
            if (card.getSuit() == Suit.BELLS && card.getRank() == Rank.SEVEN) {
                hasBellsSeven = true;
            }
        }
        
        assertTrue(hasHeartAce);
        assertTrue(hasBellsSeven);
    }

    @Test
    void testInitializeHungarianDeckForThreePlayers() {
        Deck deck = new Deck();
        deck.initializeHungarianDeckForThreePlayers();
        
        assertEquals(30, deck.size());
        
        boolean hasHeartsEight = false;
        boolean hasBellsEight = false;
        
        for (Card card : deck.getCards()) {
            if (card.getSuit() == Suit.HEARTS && card.getRank() == Rank.EIGHT) {
                hasHeartsEight = true;
            }
            if (card.getSuit() == Suit.BELLS && card.getRank() == Rank.EIGHT) {
                hasBellsEight = true;
            }
        }
        
        assertFalse(hasHeartsEight);
        assertFalse(hasBellsEight);
    }

    @Test
    void testDrawCard() {
        Deck deck = new Deck();
        deck.initializeHungarianDeck();
        int initialSize = deck.size();
        
        Card card = deck.drawCard();
        assertNotNull(card);
        assertEquals(initialSize - 1, deck.size());
        
        Deck emptyDeck = new Deck();
        assertNull(emptyDeck.drawCard());
    }

    @Test
    void testDrawCards() {
        Deck deck = new Deck();
        deck.initializeHungarianDeck();
        int initialSize = deck.size();
        
        List<Card> drawnCards = deck.drawCards(5);
        assertEquals(5, drawnCards.size());
        assertEquals(initialSize - 5, deck.size());
        
        drawnCards = deck.drawCards(50);
        assertEquals(initialSize - 5, drawnCards.size());
        assertEquals(0, deck.size());
    }

    @Test
    void testAddCard() {
        Deck deck = new Deck();
        Card card = new Card(Suit.HEARTS, Rank.ACE);
        
        deck.addCard(card);
        assertEquals(1, deck.size());
        assertEquals(card, deck.getCards().get(0));
    }

    @Test
    void testAddCards() {
        Deck deck = new Deck();
        List<Card> cards = new ArrayList<>();
        cards.add(new Card(Suit.HEARTS, Rank.ACE));
        cards.add(new Card(Suit.BELLS, Rank.KING));
        
        deck.addCards(cards);
        assertEquals(2, deck.size());
    }

    @Test
    void testShuffle() {
        Deck deck = new Deck();
        deck.initializeHungarianDeck();
        
        List<Card> originalOrder = new ArrayList<>(deck.getCards());
        deck.shuffle();
        
        boolean isDifferent = false;
        for (int i = 0; i < deck.size(); i++) {
            if (!deck.getCards().get(i).equals(originalOrder.get(i))) {
                isDifferent = true;
                break;
            }
        }
        
        assertTrue(isDifferent);
    }

    @Test
    void testGetCardsLeft() {
        Deck deck = new Deck();
        deck.initializeHungarianDeck();
        
        assertEquals(32, deck.getCardsLeft());
        deck.drawCard();
        assertEquals(31, deck.getCardsLeft());
    }

    @Test
    void testFromMap() {
        Map<String, Object> deckMap = new HashMap<>();
        List<Map<String, Object>> cardMaps = new ArrayList<>();
        
        Map<String, Object> card1 = new HashMap<>();
        card1.put("suit", "HEARTS");
        card1.put("rank", "ACE");
        cardMaps.add(card1);
        
        Map<String, Object> card2 = new HashMap<>();
        card2.put("suit", "BELLS");
        card2.put("rank", "KING");
        cardMaps.add(card2);
        
        deckMap.put("cards", cardMaps);
        
        Deck deck = Deck.fromMap(deckMap);
        assertEquals(2, deck.size());
        
        assertEquals(Suit.HEARTS, deck.getCards().get(0).getSuit());
        assertEquals(Rank.ACE, deck.getCards().get(0).getRank());
        
        assertEquals(Suit.BELLS, deck.getCards().get(1).getSuit());
        assertEquals(Rank.KING, deck.getCards().get(1).getRank());
    }

    @Test
    void testFromMapWithInvalidData() {
        assertNotNull(Deck.fromMap(null));
        assertTrue(Deck.fromMap(null).isEmpty());
        
        Map<String, Object> emptyMap = new HashMap<>();
        assertNotNull(Deck.fromMap(emptyMap));
        assertTrue(Deck.fromMap(emptyMap).isEmpty());
        
        Map<String, Object> deckMap = new HashMap<>();
        List<Map<String, Object>> cardMaps = new ArrayList<>();
        
        Map<String, Object> invalidCard = new HashMap<>();
        invalidCard.put("suit", "INVALID");
        invalidCard.put("rank", "ACE");
        cardMaps.add(invalidCard);
        
        deckMap.put("cards", cardMaps);
        
        Deck deck = Deck.fromMap(deckMap);
        assertEquals(0, deck.size());
    }
}
