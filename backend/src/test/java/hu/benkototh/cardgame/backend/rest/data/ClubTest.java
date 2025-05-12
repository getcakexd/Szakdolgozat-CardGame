package hu.benkototh.cardgame.backend.rest.data;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClubTest {

    @Test
    void testClubCreation() {
        Club club = new Club();
        assertNotNull(club);
    }

    @Test
    void testClubCreationWithParameters() {
        String name = "Card Masters";
        String description = "A club for card game enthusiasts";
        boolean isPublic = true;
        
        Club club = new Club(name, description, isPublic);
        
        assertNotNull(club);
        assertEquals(name, club.getName());
        assertEquals(description, club.getDescription());
        assertEquals(isPublic, club.isPublic());
    }

    @Test
    void testGettersAndSetters() {
        Club club = new Club();
        
        long id = 1L;
        String name = "Card Masters";
        String description = "A club for card game enthusiasts";
        boolean isPublic = true;
        
        club.setId(id);
        club.setName(name);
        club.setDescription(description);
        club.setPublic(isPublic);
        
        assertEquals(id, club.getId());
        assertEquals(name, club.getName());
        assertEquals(description, club.getDescription());
        assertEquals(isPublic, club.isPublic());
    }
}