package hu.benkototh.cardgame.backend.websocket.dto.friends;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChatMessageDTOTest {

    @Test
    void testDefaultConstructor() {
        ChatMessageDTO dto = new ChatMessageDTO();
        assertNotNull(dto);
    }

    @Test
    void testParameterizedConstructor() {
        long senderId = 1L;
        long receiverId = 2L;
        String content = "Hello, world!";
        
        ChatMessageDTO dto = new ChatMessageDTO(senderId, receiverId, content);
        
        assertNotNull(dto);
        assertEquals(senderId, dto.getSenderId());
        assertEquals(receiverId, dto.getReceiverId());
        assertEquals(content, dto.getContent());
    }

    @Test
    void testGettersAndSetters() {
        ChatMessageDTO dto = new ChatMessageDTO();
        
        long senderId = 1L;
        long receiverId = 2L;
        String content = "Hello, world!";
        
        dto.setSenderId(senderId);
        dto.setReceiverId(receiverId);
        dto.setContent(content);
        
        assertEquals(senderId, dto.getSenderId());
        assertEquals(receiverId, dto.getReceiverId());
        assertEquals(content, dto.getContent());
    }
}