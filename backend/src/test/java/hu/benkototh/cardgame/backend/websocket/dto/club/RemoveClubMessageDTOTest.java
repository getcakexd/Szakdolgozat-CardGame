package hu.benkototh.cardgame.backend.websocket.dto.club;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RemoveClubMessageDTOTest {

    @Test
    void testDefaultConstructor() {
        RemoveClubMessageDTO dto = new RemoveClubMessageDTO();
        assertNotNull(dto);
    }

    @Test
    void testParameterizedConstructor() {
        long messageId = 123L;
        
        RemoveClubMessageDTO dto = new RemoveClubMessageDTO(messageId);
        
        assertNotNull(dto);
        assertEquals(messageId, dto.getMessageId());
    }

    @Test
    void testGettersAndSetters() {
        RemoveClubMessageDTO dto = new RemoveClubMessageDTO();
        
        long messageId = 123L;
        dto.setMessageId(messageId);
        
        assertEquals(messageId, dto.getMessageId());
    }
}