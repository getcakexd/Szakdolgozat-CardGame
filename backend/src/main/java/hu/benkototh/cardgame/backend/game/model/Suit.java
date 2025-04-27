package hu.benkototh.cardgame.backend.game.model;

public enum Suit {
    HEARTS("Hearts"),
    BELLS("Bells"),
    LEAVES("Leaves"),
    ACORNS("Acorns");

    private final String name;

    Suit(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
