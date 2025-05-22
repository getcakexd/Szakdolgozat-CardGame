package hu.benkototh.cardgame.backend.rest.model;

import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class LobbyMessageTest {

    @Test
    void testLobbyMessageCreation() {
        LobbyMessage message = new LobbyMessage();
        assertNotNull(message);
        assertNotNull(message.getTimestamp());
        assertEquals("active", message.getStatus());
    }

    @Test
    void testGettersAndSetters() {
        LobbyMessage message = new LobbyMessage();
        
        long id = 1L;
        String content = "Test message";
        User user = new User();
        user.setId(1L);
        String gameId = "g-123456";
        long lobbyId = 2L;
        boolean isLobbyMessage = true;
        Date timestamp = new Date();
        String status = "deleted";
        
        message.setId(id);
        message.setContent(content);
        message.setUser(user);
        message.setGameId(gameId);
        message.setLobbyId(lobbyId);
        message.setLobbyMessage(isLobbyMessage);
        message.setTimestamp(timestamp);
        message.setStatus(status);
        
        assertEquals(id, message.getId());
        assertEquals(content, message.getContent());
        assertEquals(user, message.getUser());
        assertEquals(gameId, message.getGameId());
        assertEquals(lobbyId, message.getLobbyId());
        assertEquals(isLobbyMessage, message.isLobbyMessage());
        assertEquals(timestamp, message.getTimestamp());
        assertEquals(status, message.getStatus());
    }
}