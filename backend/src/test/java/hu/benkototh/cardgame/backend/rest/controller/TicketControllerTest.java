package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.Ticket;
import hu.benkototh.cardgame.backend.rest.Data.TicketMessage;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.ITicketMessageRepository;
import hu.benkototh.cardgame.backend.rest.repository.ITicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    @Mock
    private ITicketRepository ticketRepository;

    @Mock
    private ITicketMessageRepository ticketMessageRepository;

    @Mock
    private UserController userController;

    @Mock
    private AuditLogController auditLogController;

    @InjectMocks
    private TicketController ticketController;

    private User testUser;
    private User testAgent;
    private Ticket testTicket;
    private TicketMessage testMessage;
    private List<Ticket> tickets;
    private List<TicketMessage> messages;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole("USER");

        testAgent = new User();
        testAgent.setId(2L);
        testAgent.setUsername("testagent");
        testAgent.setEmail("agent@example.com");
        testAgent.setRole("AGENT");

        testTicket = new Ticket();
        testTicket.setId(1L);
        testTicket.setReference("ABC12345");
        testTicket.setName("Test User");
        testTicket.setEmail("test@example.com");
        testTicket.setSubject("Test Subject");
        testTicket.setCategory("Technical Support");
        testTicket.setMessage("Test message");
        testTicket.setStatus("new");
        testTicket.setUser(testUser);

        testMessage = new TicketMessage();
        testMessage.setId(1L);
        testMessage.setTicket(testTicket);
        testMessage.setUser(testUser);
        testMessage.setSenderName("Test User");
        testMessage.setSenderEmail("test@example.com");
        testMessage.setSenderType("USER");
        testMessage.setMessage("Test message");
        testMessage.setFromAgent(false);
        testMessage.setCreatedAt(new Date());

        tickets = new ArrayList<>();
        tickets.add(testTicket);

        messages = new ArrayList<>();
        messages.add(testMessage);
    }

    @Test
    void testCreateTicket() {
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);
        
        Ticket ticket = new Ticket();
        ticket.setUserId(1L);
        
        Ticket result = ticketController.createTicket(ticket);
        
        assertNotNull(result);
        assertEquals(testTicket.getId(), result.getId());
    }

    @Test
    void testCreateTicketWithoutUser() {
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);
        
        Ticket ticket = new Ticket();
        
        Ticket result = ticketController.createTicket(ticket);
        
        assertNotNull(result);
        assertEquals(testTicket.getId(), result.getId());
        verify(auditLogController).logAction(eq("TICKET_CREATED"), eq(0L), anyString());
    }

    @Test
    void testGetAllTickets() {
        when(ticketRepository.findAll()).thenReturn(tickets);
        
        List<Ticket> result = ticketController.getAllTickets();
        
        assertEquals(1, result.size());
        assertEquals(testTicket.getId(), result.get(0).getId());
    }

    @Test
    void testGetTicketsByStatus() {
        when(ticketRepository.findAll()).thenReturn(tickets);
        
        List<Ticket> result = ticketController.getTicketsByStatus("new");
        
        assertEquals(1, result.size());
        assertEquals(testTicket.getId(), result.get(0).getId());
    }

    @Test
    void testGetUserTickets() {
        when(userController.getUser(1L)).thenReturn(testUser);
        when(ticketRepository.findAll()).thenReturn(tickets);
        
        List<Ticket> result = ticketController.getUserTickets(1L);
        
        assertEquals(1, result.size());
        assertEquals(testTicket.getId(), result.get(0).getId());
    }

    @Test
    void testGetUserTicketsUserNotFound() {
        when(userController.getUser(1L)).thenReturn(null);
        
        List<Ticket> result = ticketController.getUserTickets(1L);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetTicketById() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        
        Ticket result = ticketController.getTicketById(1L);
        
        assertNotNull(result);
        assertEquals(testTicket.getId(), result.getId());
    }

    @Test
    void testGetTicketByIdNotFound() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());
        
        Ticket result = ticketController.getTicketById(1L);
        
        assertNull(result);
    }

    @Test
    void testGetTicketByReference() {
        when(ticketRepository.findAll()).thenReturn(tickets);
        
        Ticket result = ticketController.getTicketByReference("ABC12345");
        
        assertNotNull(result);
        assertEquals(testTicket.getId(), result.getId());
    }

    @Test
    void testGetTicketByReferenceNotFound() {
        when(ticketRepository.findAll()).thenReturn(tickets);
        
        Ticket result = ticketController.getTicketByReference("XYZ12345");
        
        assertNull(result);
    }

    @Test
    void testGetTicketMessages() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketMessageRepository.findAll()).thenReturn(messages);
        
        List<TicketMessage> result = ticketController.getTicketMessages(1L);
        
        assertEquals(1, result.size());
        assertEquals(testMessage.getId(), result.get(0).getId());
    }

    @Test
    void testGetTicketMessagesTicketNotFound() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());
        
        List<TicketMessage> result = ticketController.getTicketMessages(1L);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateAndAddTicketMessage() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(userController.getUser(1L)).thenReturn(testUser);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);
        when(ticketMessageRepository.save(any(TicketMessage.class))).thenReturn(testMessage);
        
        TicketMessage result = ticketController.createAndAddTicketMessage(
                1L, "Test message", "Test User", "test@example.com", "user", 1L);
        
        assertNotNull(result);
        assertEquals(testMessage.getId(), result.getId());
        verify(auditLogController).logAction(eq("TICKET_MESSAGE_ADDED"), eq(1L), anyString());
    }

    @Test
    void testCreateAndAddTicketMessageTicketNotFound() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());
        
        TicketMessage result = ticketController.createAndAddTicketMessage(
                1L, "Test message", "Test User", "test@example.com", "user", 1L);
        
        assertNull(result);
    }

    @Test
    void testAddTicketMessage() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(userController.getUser(1L)).thenReturn(testUser);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);
        when(ticketMessageRepository.save(any(TicketMessage.class))).thenReturn(testMessage);
        
        TicketMessage message = new TicketMessage();
        
        TicketMessage result = ticketController.addTicketMessage(1L, message, 1L);
        
        assertNotNull(result);
        assertEquals(testMessage.getId(), result.getId());
        verify(auditLogController).logAction(eq("TICKET_MESSAGE_ADDED"), eq(1L), anyString());
    }

    @Test
    void testAddTicketMessageTicketNotFound() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());
        
        TicketMessage message = new TicketMessage();
        
        TicketMessage result = ticketController.addTicketMessage(1L, message, 1L);
        
        assertNull(result);
    }

    @Test
    void testUpdateTicketStatus() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(userController.getUser(2L)).thenReturn(testAgent);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("ticketId", "1");
        requestData.put("status", "in_progress");
        requestData.put("agentId", "2");
        
        Ticket result = ticketController.updateTicketStatus(requestData);
        
        assertNotNull(result);
        assertEquals("in_progress", testTicket.getStatus());
        assertEquals(testAgent, testTicket.getAssignedTo());
        verify(auditLogController).logAction(eq("TICKET_STATUS_UPDATED"), eq(2L), anyString());
    }

    @Test
    void testUpdateTicketStatusMissingData() {
        Map<String, Object> requestData = new HashMap<>();
        
        Ticket result = ticketController.updateTicketStatus(requestData);
        
        assertNull(result);
    }

    @Test
    void testUpdateTicketStatusTicketNotFound() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("ticketId", "1");
        requestData.put("status", "in_progress");
        
        Ticket result = ticketController.updateTicketStatus(requestData);
        
        assertNull(result);
    }

    @Test
    void testUpdateTicketStatusAgentNotFound() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(userController.getUser(2L)).thenReturn(null);
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("ticketId", "1");
        requestData.put("status", "in_progress");
        requestData.put("agentId", "2");
        
        Ticket result = ticketController.updateTicketStatus(requestData);
        
        assertNull(result);
    }

    @Test
    void testGetAgentTickets() {
        testTicket.setAssignedTo(testAgent);
        when(userController.getUser(2L)).thenReturn(testAgent);
        when(ticketRepository.findAll()).thenReturn(tickets);
        
        List<Ticket> result = ticketController.getAgentTickets(2L);
        
        assertEquals(1, result.size());
        assertEquals(testTicket.getId(), result.get(0).getId());
    }

    @Test
    void testGetAgentTicketsAgentNotFound() {
        when(userController.getUser(2L)).thenReturn(null);
        
        List<Ticket> result = ticketController.getAgentTickets(2L);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetTicketsByEmail() {
        when(ticketRepository.findAll()).thenReturn(tickets);
        
        List<Ticket> result = ticketController.getTicketsByEmail("test@example.com");
        
        assertEquals(1, result.size());
        assertEquals(testTicket.getId(), result.get(0).getId());
    }

    @Test
    void testGetTicketsByCategory() {
        when(ticketRepository.findAll()).thenReturn(tickets);
        
        List<Ticket> result = ticketController.getTicketsByCategory("Technical Support");
        
        assertEquals(1, result.size());
        assertEquals(testTicket.getId(), result.get(0).getId());
    }

    @Test
    void testGetTicketsByDateRange() {
        Date startDate = new Date(System.currentTimeMillis() - 86400000);
        Date endDate = new Date(System.currentTimeMillis() + 86400000);
        testTicket.setCreatedAt(new Date());
        
        when(ticketRepository.findAll()).thenReturn(tickets);
        
        List<Ticket> result = ticketController.getTicketsByDateRange(startDate, endDate);
        
        assertEquals(1, result.size());
        assertEquals(testTicket.getId(), result.get(0).getId());
    }

    @Test
    void testGetMessagesByUser() {
        when(ticketMessageRepository.findAll()).thenReturn(messages);
        
        List<TicketMessage> result = ticketController.getMessagesByUser(1L);
        
        assertEquals(1, result.size());
        assertEquals(testMessage.getId(), result.get(0).getId());
    }
}