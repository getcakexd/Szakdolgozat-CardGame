package hu.benkototh.cardgame.backend.game.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private List<Card> cards;

    public Deck() {
        this.cards = new ArrayList<>();
    }

    public void initializeHungarianDeck() {
        cards.clear();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    public void initializeHungarianDeckForThreePlayers() {
        initializeHungarianDeck();
        cards.removeIf(card -> card.getRank() == Rank.EIGHT &&
                (card.getSuit() == Suit.HEARTS || card.getSuit() == Suit.BELLS));
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card drawCard() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.remove(0);
    }

    public List<Card> drawCards(int count) {
        List<Card> drawnCards = new ArrayList<>();
        for (int i = 0; i < count && !cards.isEmpty(); i++) {
            drawnCards.add(drawCard());
        }
        return drawnCards;
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public void addCards(List<Card> cardsToAdd) {
        cards.addAll(cardsToAdd);
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public List<Card> getCards() {
        return cards;
    }
}