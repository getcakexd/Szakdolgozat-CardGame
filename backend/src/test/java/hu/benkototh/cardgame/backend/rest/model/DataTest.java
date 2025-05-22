package hu.benkototh.cardgame.backend.rest.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DataTest {

    @Test
    void testDataCreation() {
        Data data = new Data();
        assertNotNull(data);
    }

    @Test
    void testGettersAndSetters() {
        Data data = new Data();
        
        long id = 1L;
        String message = "Hello, world!";
        
        data.setId(id);
        data.setMessage(message);
        
        assertEquals(id, data.getId());
        assertEquals(message, data.getMessage());
    }
}