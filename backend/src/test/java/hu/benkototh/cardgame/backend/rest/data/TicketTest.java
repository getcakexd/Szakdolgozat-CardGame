package hu.benkototh.cardgame.backend.rest.data;

import hu.benkototh.cardgame.backend.rest.Data.Ticket;
import hu.benkototh.cardgame.backend.rest.Data.TicketMessage;
import hu.benkototh.cardgame.backend.rest.Data.User;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TicketTest {

    @Test
    void testTicketCreation() {
        Ticket ticket = new Ticket();
        assertNotNull(ticket);
    }

    @Test
    void testGettersAndSetters() {
        Ticket ticket = new Ticket();
        
        long id = 1L;
        String reference = "ABC12345";
        String name = "John Doe";
        String email = "john@example.com";
        String subject = "Test Subject";
        String category = "Technical Support";
        String message = "Test message";
        String status = "Open";
        Date createdAt = new Date();
        Date updatedAt = new Date();
        User user = new User();
        User assignedTo = new User();
        List<TicketMessage> messages = new ArrayList<>();
        
        ticket.setId(id);
        ticket.setReference(reference);
        ticket.setName(name);
        ticket.setEmail(email);
        ticket.setSubject(subject);
        ticket.setCategory(category);
        ticket.setMessage(message);
        ticket.setStatus(status);
        ticket.setCreatedAt(createdAt);
        ticket.setUpdatedAt(updatedAt);
        ticket.setUser(user);
        ticket.setAssignedTo(assignedTo);
        ticket.setMessages(messages);
        
        assertEquals(id, ticket.getId());
        assertEquals(reference, ticket.getReference());
        assertEquals(name, ticket.getName());
        assertEquals(email, ticket.getEmail());
        assertEquals(subject, ticket.getSubject());
        assertEquals(category, ticket.getCategory());
        assertEquals(message, ticket.getMessage());
        assertEquals(status, ticket.getStatus());
        assertEquals(createdAt, ticket.getCreatedAt());
        assertEquals(updatedAt, ticket.getUpdatedAt());
        assertEquals(user, ticket.getUser());
        assertEquals(assignedTo, ticket.getAssignedTo());
        assertEquals(messages, ticket.getMessages());
    }

    @Test
    void testPrePersist() {
        Ticket ticket = new Ticket();
        assertNull(ticket.getCreatedAt());
        assertNull(ticket.getUpdatedAt());
        assertNull(ticket.getReference());

        ticket.onCreate();
        
        assertNotNull(ticket.getCreatedAt());
        assertNotNull(ticket.getUpdatedAt());
        assertNotNull(ticket.getReference());
        assertEquals(8, ticket.getReference().length());
    }

    @Test
    void testPreUpdate() {
        Ticket ticket = new Ticket();
        Date initialDate = new Date(System.currentTimeMillis() - 10000);
        ticket.setUpdatedAt(initialDate);

        ticket.onUpdate();
        
        assertNotEquals(initialDate, ticket.getUpdatedAt());
        assertTrue(ticket.getUpdatedAt().after(initialDate));
    }

    @Test
    void testGetUserId() {
        Ticket ticket = new Ticket();
        User user = new User();
        user.setId(1L);
        ticket.setUser(user);
        
        assertEquals(1L, ticket.getUserId());
    }

    @Test
    void testGetUserIdNull() {
        Ticket ticket = new Ticket();
        
        assertNull(ticket.getUserId());
    }

    @Test
    void testSetUserId() {
        Ticket ticket = new Ticket();
        User user = new User();
        ticket.setUser(user);
        
        ticket.setUserId(1L);
        
        assertEquals(1L, user.getId());
    }

    @Test
    void testSetAssignedToId() {
        Ticket ticket = new Ticket();
        User assignedTo = new User();
        ticket.setAssignedTo(assignedTo);
        
        ticket.setAssignedToId(1L);
        
        assertEquals(1L, assignedTo.getId());
    }
}