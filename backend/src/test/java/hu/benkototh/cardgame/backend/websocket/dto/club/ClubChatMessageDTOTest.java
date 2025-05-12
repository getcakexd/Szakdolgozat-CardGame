package hu.benkototh.cardgame.backend.websocket.dto.club;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClubChatMessageDTOTest {

    @Test
    void testDefaultConstructor() {
        ClubChatMessageDTO dto = new ClubChatMessageDTO();
        assertNotNull(dto);
    }

    @Test
    void testParameterizedConstructor() {
        long clubId = 42L;
        long senderId = 1L;
        String content = "Hello club members!";
        
        ClubChatMessageDTO dto = new ClubChatMessageDTO(clubId, senderId, content);
        
        assertNotNull(dto);
        assertEquals(clubId, dto.getClubId());
        assertEquals(senderId, dto.getSenderId());
        assertEquals(content, dto.getContent());
    }

    @Test
    void testGettersAndSetters() {
        ClubChatMessageDTO dto = new ClubChatMessageDTO();
        
        long clubId = 42L;
        long senderId = 1L;
        String content = "Hello club members!";
        
        dto.setClubId(clubId);
        dto.setSenderId(senderId);
        dto.setContent(content);
        
        assertEquals(clubId, dto.getClubId());
        assertEquals(senderId, dto.getSenderId());
        assertEquals(content, dto.getContent());
    }
}