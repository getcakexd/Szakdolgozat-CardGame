package hu.benkototh.cardgame.backend.websocket.dto.lobby;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GetLobbyMessagesDTOTest {

    @Test
    void testDefaultConstructor() {
        GetLobbyMessagesDTO dto = new GetLobbyMessagesDTO();
        assertNotNull(dto);
    }

    @Test
    void testGettersAndSetters() {
        GetLobbyMessagesDTO dto = new GetLobbyMessagesDTO();
        
        long lobbyId = 42L;
        String gameId = "g-123456";
        
        dto.setLobbyId(lobbyId);
        dto.setGameId(gameId);
        
        assertEquals(lobbyId, dto.getLobbyId());
        assertEquals(gameId, dto.getGameId());
    }
}