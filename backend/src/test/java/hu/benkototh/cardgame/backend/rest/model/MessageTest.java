package hu.benkototh.cardgame.backend.rest.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    @Test
    void testMessageCreation() {
        Message message = new Message();
        assertNotNull(message);
    }

    @Test
    void testMessageCreationWithParameters() {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");

        String content = "Hello, how are you?";

        Message message = new Message(sender, receiver, content);
        
        assertNotNull(message);
        assertEquals(sender, message.getSender());
        assertEquals(receiver, message.getReceiver());
        assertEquals(content, message.getContent());
        assertNotNull(message.getTimestamp());
        assertEquals("unread", message.getStatus());
    }

    @Test
    void testGettersAndSetters() {
        Message message = new Message();
        
        Long id = 1L;
        User sender = new User();
        sender.setId(1L);
        User receiver = new User();
        receiver.setId(2L);
        String content = "Test message";
        LocalDateTime timestamp = LocalDateTime.now();
        String status = "read";
        
        message.setId(id);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setTimestamp(timestamp);
        message.setStatus(status);
        
        assertEquals(id, message.getId());
        assertEquals(sender, message.getSender());
        assertEquals(receiver, message.getReceiver());
        assertEquals(content, message.getContent());
        assertEquals(timestamp, message.getTimestamp());
        assertEquals(status, message.getStatus());
    }
}