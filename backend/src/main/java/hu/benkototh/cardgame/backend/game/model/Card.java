package hu.benkototh.cardgame.backend.game.model;

public class Card {
    private Suit suit;
    private Rank rank;
    private int value;
    private boolean isSeven;

    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
        this.value = rank.getValue();
        this.isSeven = rank == Rank.SEVEN;
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

    public boolean isSeven() {
        return isSeven;
    }

    public void setSeven(boolean seven) {
        isSeven = seven;
    }
}
