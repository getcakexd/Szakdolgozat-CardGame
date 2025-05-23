package hu.benkototh.cardgame.backend.websocket.dto.club;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GetClubMessagesDTOTest {

    @Test
    void testDefaultConstructor() {
        GetClubMessagesDTO dto = new GetClubMessagesDTO();
        assertNotNull(dto);
    }

    @Test
    void testParameterizedConstructor() {
        long clubId = 42L;
        
        GetClubMessagesDTO dto = new GetClubMessagesDTO(clubId);
        
        assertNotNull(dto);
        assertEquals(clubId, dto.getClubId());
    }

    @Test
    void testGettersAndSetters() {
        GetClubMessagesDTO dto = new GetClubMessagesDTO();
        
        long clubId = 42L;
        dto.setClubId(clubId);
        
        assertEquals(clubId, dto.getClubId());
    }
}