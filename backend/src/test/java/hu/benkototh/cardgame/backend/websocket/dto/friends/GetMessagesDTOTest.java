package hu.benkototh.cardgame.backend.websocket.dto.friends;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GetMessagesDTOTest {

    @Test
    void testDefaultConstructor() {
        GetMessagesDTO dto = new GetMessagesDTO();
        assertNotNull(dto);
    }

    @Test
    void testParameterizedConstructor() {
        long userId = 1L;
        long friendId = 2L;
        GetMessagesDTO dto = new GetMessagesDTO(userId, friendId);
        
        assertNotNull(dto);
        assertEquals(userId, dto.getUserId());
        assertEquals(friendId, dto.getFriendId());
    }

    @Test
    void testGettersAndSetters() {
        GetMessagesDTO dto = new GetMessagesDTO();
        
        long userId = 1L;
        long friendId = 2L;
        
        dto.setUserId(userId);
        dto.setFriendId(friendId);
        
        assertEquals(userId, dto.getUserId());
        assertEquals(friendId, dto.getFriendId());
    }
}