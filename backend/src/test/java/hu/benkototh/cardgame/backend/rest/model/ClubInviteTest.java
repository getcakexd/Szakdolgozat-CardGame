package hu.benkototh.cardgame.backend.rest.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClubInviteTest {

    @Test
    void testClubInviteCreation() {
        ClubInvite invite = new ClubInvite();
        assertNotNull(invite);
    }

    @Test
    void testClubInviteCreationWithParameters() {
        Club club = new Club("Card Masters", "A club for card game enthusiasts", true);
        club.setId(1L);
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        
        ClubInvite invite = new ClubInvite(club, user);
        
        assertNotNull(invite);
        assertEquals(club, invite.getClub());
        assertEquals(user, invite.getUser());
        assertEquals("pending", invite.getStatus());
    }

    @Test
    void testGettersAndSetters() {
        ClubInvite invite = new ClubInvite();
        
        String status = "accepted";
        String clubName = "Card Masters";
        String username = "testuser";
        
        invite.setStatus(status);
        invite.setClubName(clubName);
        invite.setUsername(username);
        
        assertEquals(status, invite.getStatus());
        assertEquals(clubName, invite.getClubName());
        assertEquals(username, invite.getUsername());
    }
}