package hu.benkototh.cardgame.backend.game.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Embeddable
public class Card {
    @Enumerated(EnumType.STRING)
    private Suit suit;

    @Enumerated(EnumType.STRING)
    private Rank rank;

    private int value;

    public Card() {
    }

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
        this.value = calculateValue();
    }

    @JsonCreator
    public static Card create(
            @JsonProperty("suit") String suitStr,
            @JsonProperty("rank") String rankStr) {

        if (suitStr == null || rankStr == null) {
            return null;
        }

        try {
            Suit suit = Suit.valueOf(suitStr);
            Rank rank = Rank.valueOf(rankStr);
            return new Card(suit, rank);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Card fromMap(java.util.Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        String suitStr = (String) map.get("suit");
        String rankStr = (String) map.get("rank");

        if (suitStr != null && rankStr != null) {
            try {
                Suit suit = Suit.valueOf(suitStr);
                Rank rank = Rank.valueOf(rankStr);
                return new Card(suit, rank);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        return null;
    }

    private int calculateValue() {
        return switch (rank) {
            case ACE, TEN -> 10;
            default -> 0;
        };
    }

    public Suit getSuit() {
        return suit;
    }

    public void setSuit(Suit suit) {
        this.suit = suit;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Card other = (Card) obj;
        return suit == other.suit && rank == other.rank;
    }

    @Override
    public int hashCode() {
        return 31 * suit.hashCode() + rank.hashCode();
    }
}
