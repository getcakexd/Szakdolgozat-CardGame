package hu.benkototh.cardgame.backend.game.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RankTest {

    @Test
    void testRankValues() {
        assertEquals(8, Rank.values().length);
        assertEquals(Rank.SEVEN, Rank.valueOf("SEVEN"));
        assertEquals(Rank.EIGHT, Rank.valueOf("EIGHT"));
        assertEquals(Rank.NINE, Rank.valueOf("NINE"));
        assertEquals(Rank.TEN, Rank.valueOf("TEN"));
        assertEquals(Rank.UNDER, Rank.valueOf("UNDER"));
        assertEquals(Rank.OVER, Rank.valueOf("OVER"));
        assertEquals(Rank.KING, Rank.valueOf("KING"));
        assertEquals(Rank.ACE, Rank.valueOf("ACE"));
    }

    @Test
    void testGetName() {
        assertEquals("7", Rank.SEVEN.getName());
        assertEquals("8", Rank.EIGHT.getName());
        assertEquals("9", Rank.NINE.getName());
        assertEquals("10", Rank.TEN.getName());
        assertEquals("Under", Rank.UNDER.getName());
        assertEquals("Over", Rank.OVER.getName());
        assertEquals("KING", Rank.KING.getName());
        assertEquals("ACE", Rank.ACE.getName());
    }

    @Test
    void testGetValue() {
        assertEquals(0, Rank.SEVEN.getValue());
        assertEquals(0, Rank.EIGHT.getValue());
        assertEquals(0, Rank.NINE.getValue());
        assertEquals(10, Rank.TEN.getValue());
        assertEquals(0, Rank.UNDER.getValue());
        assertEquals(0, Rank.OVER.getValue());
        assertEquals(0, Rank.KING.getValue());
        assertEquals(10, Rank.ACE.getValue());
    }

    @Test
    void testToString() {
        assertEquals("7", Rank.SEVEN.toString());
        assertEquals("8", Rank.EIGHT.toString());
        assertEquals("9", Rank.NINE.toString());
        assertEquals("10", Rank.TEN.toString());
        assertEquals("Under", Rank.UNDER.toString());
        assertEquals("Over", Rank.OVER.toString());
        assertEquals("KING", Rank.KING.toString());
        assertEquals("ACE", Rank.ACE.toString());
    }
}
