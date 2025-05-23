package hu.benkototh.cardgame.backend.websocket.dto.club;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UnsendClubMessageDTOTest {

    @Test
    void testDefaultConstructor() {
        UnsendClubMessageDTO dto = new UnsendClubMessageDTO();
        assertNotNull(dto);
    }

    @Test
    void testParameterizedConstructor() {
        long messageId = 123L;
        
        UnsendClubMessageDTO dto = new UnsendClubMessageDTO(messageId);
        
        assertNotNull(dto);
        assertEquals(messageId, dto.getMessageId());
    }

    @Test
    void testGettersAndSetters() {
        UnsendClubMessageDTO dto = new UnsendClubMessageDTO();
        
        long messageId = 123L;
        dto.setMessageId(messageId);
        
        assertEquals(messageId, dto.getMessageId());
    }
}