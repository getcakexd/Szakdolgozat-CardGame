package hu.benkototh.cardgame.backend.websocket.dto.lobby;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UnsendLobbyMessageDTOTest {

    @Test
    void testDefaultConstructor() {
        UnsendLobbyMessageDTO dto = new UnsendLobbyMessageDTO();
        assertNotNull(dto);
    }

    @Test
    void testGettersAndSetters() {
        UnsendLobbyMessageDTO dto = new UnsendLobbyMessageDTO();
        
        long messageId = 123L;
        dto.setMessageId(messageId);
        
        assertEquals(messageId, dto.getMessageId());
    }
}