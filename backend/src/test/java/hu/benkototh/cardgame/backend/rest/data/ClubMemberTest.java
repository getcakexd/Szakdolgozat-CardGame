package hu.benkototh.cardgame.backend.rest.data;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.ClubMember;
import hu.benkototh.cardgame.backend.rest.Data.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClubMemberTest {

    @Test
    void testClubMemberCreation() {
        ClubMember member = new ClubMember();
        assertNotNull(member);
    }

    @Test
    void testClubMemberCreationWithParameters() {
        Club club = new Club("Card Masters", "A club for card game enthusiasts", true);
        club.setId(1L);
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        
        ClubMember member = new ClubMember(club, user);
        
        assertNotNull(member);
        assertEquals(club, member.getClub());
        assertEquals(user, member.getUser());
        assertEquals("member", member.getRole());
    }

    @Test
    void testClubMemberCreationWithRole() {
        Club club = new Club("Card Masters", "A club for card game enthusiasts", true);
        club.setId(1L);
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        
        String role = "admin";
        
        ClubMember member = new ClubMember(club, user, role);
        
        assertNotNull(member);
        assertEquals(club, member.getClub());
        assertEquals(user, member.getUser());
        assertEquals(role, member.getRole());
    }

    @Test
    void testGettersAndSetters() {
        ClubMember member = new ClubMember();
        
        long id = 1L;
        Club club = new Club();
        User user = new User();
        String role = "moderator";
        String username = "testuser";
        
        member.setId(id);
        member.setClub(club);
        member.setUser(user);
        member.setRole(role);
        member.setUsername(username);
        
        assertEquals(id, member.getId());
        assertEquals(club, member.getClub());
        assertEquals(user, member.getUser());
        assertEquals(role, member.getRole());
        assertEquals(username, member.getUsername());
    }
}