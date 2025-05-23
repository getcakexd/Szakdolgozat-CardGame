package hu.benkototh.cardgame.backend.rest.model;

import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class TicketMessageTest {

    @Test
    void testTicketMessageCreation() {
        TicketMessage message = new TicketMessage();
        assertNotNull(message);
    }

    @Test
    void testGettersAndSetters() {
        TicketMessage message = new TicketMessage();
        
        long id = 1L;
        Ticket ticket = new Ticket();
        User user = new User();
        String senderName = "John Doe";
        String senderEmail = "john@example.com";
        String senderType = "USER";
        String messageContent = "Test message";
        boolean isFromAgent = false;
        Date createdAt = new Date();
        
        message.setId(id);
        message.setTicket(ticket);
        message.setUser(user);
        message.setSenderName(senderName);
        message.setSenderEmail(senderEmail);
        message.setSenderType(senderType);
        message.setMessage(messageContent);
        message.setFromAgent(isFromAgent);
        message.setCreatedAt(createdAt);
        
        assertEquals(id, message.getId());
        assertEquals(ticket, message.getTicket());
        assertEquals(user, message.getUser());
        assertEquals(senderName, message.getSenderName());
        assertEquals(senderEmail, message.getSenderEmail());
        assertEquals(senderType, message.getSenderType());
        assertEquals(messageContent, message.getMessage());
        assertEquals(isFromAgent, message.isFromAgent());
        assertEquals(createdAt, message.getCreatedAt());
    }

    @Test
    void testTicketIdSetter() {
        TicketMessage message = new TicketMessage();
        Ticket ticket = new Ticket();
        message.setTicket(ticket);
        
        message.setTicketId(1L);
        
        assertEquals(1L, ticket.getId());
    }

    @Test
    void testUserIdSetter() {
        TicketMessage message = new TicketMessage();
        User user = new User();
        message.setUser(user);
        
        message.setUserId(1L);
        
        assertEquals(1L, user.getId());
    }

    @Test
    void testPrePersist() {
        TicketMessage message = new TicketMessage();
        assertNull(message.getCreatedAt());

        message.onCreate();
        
        assertNotNull(message.getCreatedAt());
    }
}