package hu.benkototh.cardgame.backend.game.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
@JsonDeserialize
public class Deck implements Serializable {
    private List<Card> cards;

    public Deck() {
        this.cards = new ArrayList<>();
    }

    @JsonCreator
    public Deck(@JsonProperty("cards") List<Card> cards) {
        this.cards = cards != null ? cards : new ArrayList<>();
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

    @JsonProperty("cards")
    public List<Card> getCards() {
        return cards;
    }

    @JsonProperty("cards")
    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public static Deck fromMap(java.util.Map<String, Object> map) {
        if (map == null) {
            return new Deck();
        }

        @SuppressWarnings("unchecked")
        List<java.util.Map<String, Object>> cardMaps = (List<java.util.Map<String, Object>>) map.get("cards");

        if (cardMaps == null) {
            return new Deck();
        }

        List<Card> cards = new ArrayList<>();
        for (java.util.Map<String, Object> cardMap : cardMaps) {
            String suitStr = (String) cardMap.get("suit");
            String rankStr = (String) cardMap.get("rank");

            if (suitStr != null && rankStr != null) {
                try {
                    Suit suit = Suit.valueOf(suitStr);
                    Rank rank = Rank.valueOf(rankStr);
                    cards.add(new Card(suit, rank));
                } catch (IllegalArgumentException e) {
                }
            }
        }

        return new Deck(cards);
    }

    public int getCardsLeft() {
        return cards.size();
    }
}
