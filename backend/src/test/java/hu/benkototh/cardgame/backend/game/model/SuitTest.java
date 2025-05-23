package hu.benkototh.cardgame.backend.game.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SuitTest {

    @Test
    void testSuitValues() {
        assertEquals(4, Suit.values().length);
        assertEquals(Suit.HEARTS, Suit.valueOf("HEARTS"));
        assertEquals(Suit.BELLS, Suit.valueOf("BELLS"));
        assertEquals(Suit.LEAVES, Suit.valueOf("LEAVES"));
        assertEquals(Suit.ACORNS, Suit.valueOf("ACORNS"));
    }

    @Test
    void testGetName() {
        assertEquals("Hearts", Suit.HEARTS.getName());
        assertEquals("Bells", Suit.BELLS.getName());
        assertEquals("Leaves", Suit.LEAVES.getName());
        assertEquals("Acorns", Suit.ACORNS.getName());
    }

    @Test
    void testToString() {
        assertEquals("Hearts", Suit.HEARTS.toString());
        assertEquals("Bells", Suit.BELLS.toString());
        assertEquals("Leaves", Suit.LEAVES.toString());
        assertEquals("Acorns", Suit.ACORNS.toString());
    }
}
