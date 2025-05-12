package hu.benkototh.cardgame.backend.rest.data;

import hu.benkototh.cardgame.backend.rest.Data.Club;
import hu.benkototh.cardgame.backend.rest.Data.ClubMessage;
import hu.benkototh.cardgame.backend.rest.Data.User;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ClubMessageTest {

    @Test
    void testClubMessageCreation() {
        ClubMessage message = new ClubMessage();
        assertNotNull(message);
    }

    @Test
    void testClubMessageCreationWithParameters() {
        Club club = new Club("Card Masters", "A club for card game enthusiasts", true);
        club.setId(1L);
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        
        String content = "Hello club members!";
        
        ClubMessage message = new ClubMessage(club, user, content);
        
        assertNotNull(message);
        assertEquals(club, message.getClub());
        assertEquals(user, message.getSender());
        assertEquals(content, message.getContent());
        assertNotNull(message.getTimestamp());
        assertEquals("sent", message.getStatus());
    }

    @Test
    void testClubMessageCreationWithTimestamp() {
        Club club = new Club("Card Masters", "A club for card game enthusiasts", true);
        club.setId(1L);
        
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        
        String content = "Hello club members!";
        LocalDateTime timestamp = LocalDateTime.now();
        
        ClubMessage message = new ClubMessage(club, user, content, timestamp);
        
        assertNotNull(message);
        assertEquals(club, message.getClub());
        assertEquals(user, message.getSender());
        assertEquals(content, message.getContent());
        assertEquals(timestamp, message.getTimestamp());
        assertEquals("sent", message.getStatus());
    }

    @Test
    void testGettersAndSetters() {
        ClubMessage message = new ClubMessage();
        
        long id = 1L;
        Club club = new Club();
        User sender = new User();
        String content = "Test message";
        LocalDateTime timestamp = LocalDateTime.now();
        String status = "edited";
        
        message.setId(id);
        message.setClub(club);
        message.setSender(sender);
        message.setContent(content);
        message.setTimestamp(timestamp);
        message.setStatus(status);
        
        assertEquals(id, message.getId());
        assertEquals(club, message.getClub());
        assertEquals(sender, message.getSender());
        assertEquals(content, message.getContent());
        assertEquals(timestamp, message.getTimestamp());
        assertEquals(status, message.getStatus());
    }
}