package hu.benkototh.cardgame.backend.websocket.dto.lobby;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LobbyChatMessageDTOTest {

    @Test
    void testDefaultConstructor() {
        LobbyChatMessageDTO dto = new LobbyChatMessageDTO();
        assertNotNull(dto);
    }

    @Test
    void testGettersAndSetters() {
        LobbyChatMessageDTO dto = new LobbyChatMessageDTO();
        
        long senderId = 1L;
        String content = "Hello everyone in the lobby!";
        long lobbyId = 42L;
        String gameId = "g-123456";
        
        dto.setSenderId(senderId);
        dto.setContent(content);
        dto.setLobbyId(lobbyId);
        dto.setGameId(gameId);
        
        assertEquals(senderId, dto.getSenderId());
        assertEquals(content, dto.getContent());
        assertEquals(lobbyId, dto.getLobbyId());
        assertEquals(gameId, dto.getGameId());
    }
}