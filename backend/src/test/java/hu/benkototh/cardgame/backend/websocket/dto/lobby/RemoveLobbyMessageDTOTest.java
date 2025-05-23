package hu.benkototh.cardgame.backend.websocket.dto.lobby;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RemoveLobbyMessageDTOTest {

    @Test
    void testDefaultConstructor() {
        RemoveLobbyMessageDTO dto = new RemoveLobbyMessageDTO();
        assertNotNull(dto);
    }

    @Test
    void testGettersAndSetters() {
        RemoveLobbyMessageDTO dto = new RemoveLobbyMessageDTO();
        
        long messageId = 123L;
        dto.setMessageId(messageId);
        
        assertEquals(messageId, dto.getMessageId());
    }
}