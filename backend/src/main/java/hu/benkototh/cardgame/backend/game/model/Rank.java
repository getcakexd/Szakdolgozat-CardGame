package hu.benkototh.cardgame.backend.game.model;

public enum Rank {
    SEVEN("7", 0),
    EIGHT("8", 0),
    NINE("9", 0),
    TEN("10", 10),
    UNDER("Under", 0),
    OVER("Over", 0),
    KING("KING", 0),
    ACE("ACE", 10);

    private final String name;
    private final int value;

    Rank(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name;
    }
}
