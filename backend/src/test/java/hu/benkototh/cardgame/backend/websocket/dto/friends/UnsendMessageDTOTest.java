package hu.benkototh.cardgame.backend.websocket.dto.friends;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UnsendMessageDTOTest {

    @Test
    void testDefaultConstructor() {
        UnsendMessageDTO dto = new UnsendMessageDTO();
        assertNotNull(dto);
    }

    @Test
    void testParameterizedConstructor() {
        long messageId = 123L;
        UnsendMessageDTO dto = new UnsendMessageDTO(messageId);
        
        assertNotNull(dto);
        assertEquals(messageId, dto.getMessageId());
    }

    @Test
    void testGettersAndSetters() {
        UnsendMessageDTO dto = new UnsendMessageDTO();
        
        long messageId = 123L;
        dto.setMessageId(messageId);
        
        assertEquals(messageId, dto.getMessageId());
    }
}