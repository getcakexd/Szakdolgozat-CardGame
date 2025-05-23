package hu.benkototh.cardgame.backend.websocket.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserIdDTOTest {

    @Test
    void testDefaultConstructor() {
        UserIdDTO dto = new UserIdDTO();
        assertNotNull(dto);
    }

    @Test
    void testParameterizedConstructor() {
        long userId = 1L;
        UserIdDTO dto = new UserIdDTO(userId);
        
        assertNotNull(dto);
        assertEquals(userId, dto.getUserId());
    }

    @Test
    void testGettersAndSetters() {
        UserIdDTO dto = new UserIdDTO();
        
        long userId = 1L;
        dto.setUserId(userId);
        
        assertEquals(userId, dto.getUserId());
    }
}