package hu.benkototh.cardgame.backend.game.model;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class ClubMemberStatsTest {

    private ClubMemberStats clubMemberStats;
    private Club club;
    private User user;

    @BeforeEach
    void setUp() {
        clubMemberStats = new ClubMemberStats();
        club = new Club();
        club.setId(1L);
        club.setName("Test Club");
        
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
    }

    @Test
    void testInitialValues() {
        assertEquals(0, clubMemberStats.getGamesPlayed());
        assertEquals(0, clubMemberStats.getGamesWon());
        assertEquals(0, clubMemberStats.getGamesDrawn());
        assertEquals(0, clubMemberStats.getTotalPoints());
        assertEquals(0, clubMemberStats.getHighestScore());
        assertEquals(0, clubMemberStats.getTotalFatsCollected());
        assertNotNull(clubMemberStats.getLastPlayed());
    }

    @Test
    void testSettersAndGetters() {
        Date now = new Date();
        
        clubMemberStats.setId(1L);
        clubMemberStats.setClub(club);
        clubMemberStats.setUser(user);
        clubMemberStats.setGamesPlayed(10);
        clubMemberStats.setGamesWon(5);
        clubMemberStats.setGamesDrawn(2);
        clubMemberStats.setTotalPoints(500);
        clubMemberStats.setHighestScore(120);
        clubMemberStats.setTotalFatsCollected(30);
        clubMemberStats.setLastPlayed(now);
        
        assertEquals(1L, clubMemberStats.getId());
        assertEquals(club, clubMemberStats.getClub());
        assertEquals(user, clubMemberStats.getUser());
        assertEquals(10, clubMemberStats.getGamesPlayed());
        assertEquals(5, clubMemberStats.getGamesWon());
        assertEquals(2, clubMemberStats.getGamesDrawn());
        assertEquals(500, clubMemberStats.getTotalPoints());
        assertEquals(120, clubMemberStats.getHighestScore());
        assertEquals(30, clubMemberStats.getTotalFatsCollected());
        assertEquals(now, clubMemberStats.getLastPlayed());
    }

    @Test
    void testIncrementGamesPlayed() {
        Date before = clubMemberStats.getLastPlayed();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        clubMemberStats.incrementGamesPlayed();
        assertEquals(1, clubMemberStats.getGamesPlayed());
        assertTrue(clubMemberStats.getLastPlayed().after(before));
    }

    @Test
    void testIncrementGamesWon() {
        Date before = clubMemberStats.getLastPlayed();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        clubMemberStats.incrementGamesWon();
        assertEquals(1, clubMemberStats.getGamesWon());
        assertTrue(clubMemberStats.getLastPlayed().after(before));
    }

    @Test
    void testIncrementGamesDrawn() {
        Date before = clubMemberStats.getLastPlayed();
        
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        clubMemberStats.incrementGamesDrawn();
        assertEquals(1, clubMemberStats.getGamesDrawn());
        assertTrue(clubMemberStats.getLastPlayed().after(before));
    }

    @Test
    void testAddPoints() {
        clubMemberStats.addPoints(100);
        assertEquals(100, clubMemberStats.getTotalPoints());
        assertEquals(100, clubMemberStats.getHighestScore());
        
        clubMemberStats.addPoints(50);
        assertEquals(150, clubMemberStats.getTotalPoints());
        assertEquals(100, clubMemberStats.getHighestScore());
        
        clubMemberStats.addPoints(120);
        assertEquals(270, clubMemberStats.getTotalPoints());
        assertEquals(120, clubMemberStats.getHighestScore());
    }

    @Test
    void testAddFatsCollected() {
        clubMemberStats.addFatsCollected(5);
        assertEquals(5, clubMemberStats.getTotalFatsCollected());
        
        clubMemberStats.addFatsCollected(3);
        assertEquals(8, clubMemberStats.getTotalFatsCollected());
    }
}
