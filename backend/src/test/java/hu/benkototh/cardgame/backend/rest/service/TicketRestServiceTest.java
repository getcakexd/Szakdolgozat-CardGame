package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.model.Ticket;
import hu.benkototh.cardgame.backend.rest.model.TicketMessage;
import hu.benkototh.cardgame.backend.rest.model.User;
import hu.benkototh.cardgame.backend.rest.controller.TicketController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketRestServiceTest {

    @Mock
    private TicketController ticketController;

    @InjectMocks
    private TicketRestService ticketRestService;

    private User testUser;
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
        when(ticketController.createTicket(any(Ticket.class))).thenReturn(testTicket);
        
        ResponseEntity<Ticket> response = ticketRestService.createTicket(new Ticket());
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testTicket.getId(), response.getBody().getId());
    }

    @Test
    void testGetTicketByIdSuccess() {
        when(ticketController.getTicketById(1L)).thenReturn(testTicket);
        
        ResponseEntity<?> response = ticketRestService.getTicketById(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testTicket, response.getBody());
    }

    @Test
    void testGetTicketByIdNotFound() {
        when(ticketController.getTicketById(1L)).thenReturn(null);
        
        ResponseEntity<?> response = ticketRestService.getTicketById(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Ticket not found", responseBody.get("message"));
    }

    @Test
    void testGetTicketByReferenceSuccess() {
        when(ticketController.getTicketByReference("ABC12345")).thenReturn(testTicket);
        
        ResponseEntity<?> response = ticketRestService.getTicketByReference("ABC12345");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testTicket, response.getBody());
    }

    @Test
    void testGetTicketByReferenceNotFound() {
        when(ticketController.getTicketByReference("ABC12345")).thenReturn(null);
        
        ResponseEntity<?> response = ticketRestService.getTicketByReference("ABC12345");
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Ticket not found", responseBody.get("message"));
    }

    @Test
    void testGetUserTickets() {
        when(ticketController.getUserTickets(1L)).thenReturn(tickets);
        
        ResponseEntity<List<Ticket>> response = ticketRestService.getUserTickets(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testTicket.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testGetTicketMessagesById() {
        when(ticketController.getTicketById(1L)).thenReturn(testTicket);
        when(ticketController.getTicketMessages(1L)).thenReturn(messages);
        
        ResponseEntity<?> response = ticketRestService.getTicketMessagesById(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<TicketMessage> responseBody = (List<TicketMessage>) response.getBody();
        assertEquals(1, responseBody.size());
        assertEquals(testMessage.getId(), responseBody.get(0).getId());
    }

    @Test
    void testGetTicketMessagesByIdTicketNotFound() {
        when(ticketController.getTicketById(1L)).thenReturn(null);
        
        ResponseEntity<?> response = ticketRestService.getTicketMessagesById(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Ticket not found", responseBody.get("message"));
    }

    @Test
    void testAddTicketMessageSuccess() {
        when(ticketController.getTicketById(1L)).thenReturn(testTicket);
        when(ticketController.createAndAddTicketMessage(eq(1L), anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(testMessage);
        
        ResponseEntity<?> response = ticketRestService.addTicketMessage(1L, "Test message", "Test User", "test@example.com", "USER", 1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testMessage, response.getBody());
    }

    @Test
    void testAddTicketMessageTicketNotFound() {
        when(ticketController.getTicketById(1L)).thenReturn(null);
        
        ResponseEntity<?> response = ticketRestService.addTicketMessage(1L, "Test message", "Test User", "test@example.com", "USER", 1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Ticket not found with ID: 1", responseBody.get("message"));
    }

    @Test
    void testAddTicketMessageFailure() {
        when(ticketController.getTicketById(1L)).thenReturn(testTicket);
        when(ticketController.createAndAddTicketMessage(eq(1L), anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(null);
        
        ResponseEntity<?> response = ticketRestService.addTicketMessage(1L, "Test message", "Test User", "test@example.com", "USER", 1L);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Failed to add message", responseBody.get("message"));
    }

    @Test
    void testAddTicketMessageException() {
        when(ticketController.getTicketById(1L)).thenReturn(testTicket);
        when(ticketController.createAndAddTicketMessage(eq(1L), anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenThrow(new RuntimeException("Test exception"));
        
        ResponseEntity<?> response = ticketRestService.addTicketMessage(1L, "Test message", "Test User", "test@example.com", "USER", 1L);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Error processing request: Test exception", responseBody.get("message"));
    }

    @Test
    void testGetTicketMessages() {
        when(ticketController.getTicketById(1L)).thenReturn(testTicket);
        when(ticketController.getTicketMessages(1L)).thenReturn(messages);
        
        ResponseEntity<?> response = ticketRestService.getTicketMessages(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof List);
        List<TicketMessage> responseBody = (List<TicketMessage>) response.getBody();
        assertEquals(1, responseBody.size());
        assertEquals(testMessage.getId(), responseBody.get(0).getId());
    }

    @Test
    void testGetTicketMessagesTicketNotFound() {
        when(ticketController.getTicketById(1L)).thenReturn(null);
        
        ResponseEntity<?> response = ticketRestService.getTicketMessages(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Ticket not found", responseBody.get("message"));
    }

    @Test
    void testAddTicketMessageAlternativeSuccess() {
        when(ticketController.getTicketById(1L)).thenReturn(testTicket);
        when(ticketController.addTicketMessage(eq(1L), any(TicketMessage.class), anyLong()))
                .thenReturn(testMessage);
        
        ResponseEntity<?> response = ticketRestService.addTicketMessage(1L, new TicketMessage(), 1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testMessage, response.getBody());
    }

    @Test
    void testAddTicketMessageAlternativeTicketNotFound() {
        when(ticketController.getTicketById(1L)).thenReturn(null);
        
        ResponseEntity<?> response = ticketRestService.addTicketMessage(1L, new TicketMessage(), 1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Ticket not found", responseBody.get("message"));
    }

    @Test
    void testAddTicketMessageAlternativeFailure() {
        when(ticketController.getTicketById(1L)).thenReturn(testTicket);
        when(ticketController.addTicketMessage(eq(1L), any(TicketMessage.class), anyLong()))
                .thenReturn(null);
        
        ResponseEntity<?> response = ticketRestService.addTicketMessage(1L, new TicketMessage(), 1L);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Failed to add message", responseBody.get("message"));
    }

    @Test
    void testGetTicketsByEmail() {
        when(ticketController.getTicketsByEmail("test@example.com")).thenReturn(tickets);
        
        ResponseEntity<List<Ticket>> response = ticketRestService.getTicketsByEmail("test@example.com");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testTicket.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testGetTicketsByCategory() {
        when(ticketController.getTicketsByCategory("Technical Support")).thenReturn(tickets);
        
        ResponseEntity<List<Ticket>> response = ticketRestService.getTicketsByCategory("Technical Support");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testTicket.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testGetTicketsByDateRange() {
        Date startDate = new Date();
        Date endDate = new Date();
        when(ticketController.getTicketsByDateRange(startDate, endDate)).thenReturn(tickets);
        
        ResponseEntity<List<Ticket>> response = ticketRestService.getTicketsByDateRange(startDate, endDate);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testTicket.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testAgentGetAllTickets() {
        TicketRestService.AgentTicketRestService agentService = new TicketRestService.AgentTicketRestService();
        ReflectionTestUtils.setField(agentService, "ticketController", ticketController);
        
        when(ticketController.getAllTickets()).thenReturn(tickets);
        
        ResponseEntity<List<Ticket>> response = agentService.getAllTickets();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testTicket.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testAgentGetTicketsByStatus() {
        TicketRestService.AgentTicketRestService agentService = new TicketRestService.AgentTicketRestService();
        ReflectionTestUtils.setField(agentService, "ticketController", ticketController);
        
        when(ticketController.getTicketsByStatus("new")).thenReturn(tickets);
        
        ResponseEntity<List<Ticket>> response = agentService.getTicketsByStatus("new");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testTicket.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testAgentGetAgentTickets() {
        TicketRestService.AgentTicketRestService agentService = new TicketRestService.AgentTicketRestService();
        ReflectionTestUtils.setField(agentService, "ticketController", ticketController);
        
        when(ticketController.getAgentTickets(1L)).thenReturn(tickets);
        
        ResponseEntity<List<Ticket>> response = agentService.getAgentTickets(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testTicket.getId(), response.getBody().get(0).getId());
    }

    @Test
    void testAgentGetTicket() {
        TicketRestService.AgentTicketRestService agentService = new TicketRestService.AgentTicketRestService();
        ReflectionTestUtils.setField(agentService, "ticketController", ticketController);
        
        when(ticketController.getTicketById(1L)).thenReturn(testTicket);
        
        ResponseEntity<?> response = agentService.getTicket(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testTicket, response.getBody());
    }

    @Test
    void testAgentGetTicketNotFound() {
        TicketRestService.AgentTicketRestService agentService = new TicketRestService.AgentTicketRestService();
        ReflectionTestUtils.setField(agentService, "ticketController", ticketController);
        
        when(ticketController.getTicketById(1L)).thenReturn(null);
        
        ResponseEntity<?> response = agentService.getTicket(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Ticket not found", responseBody.get("message"));
    }

    @Test
    void testAgentUpdateTicketStatusSuccess() {
        TicketRestService.AgentTicketRestService agentService = new TicketRestService.AgentTicketRestService();
        ReflectionTestUtils.setField(agentService, "ticketController", ticketController);
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("ticketId", "1");
        requestData.put("status", "in_progress");
        
        when(ticketController.updateTicketStatus(requestData)).thenReturn(testTicket);
        
        ResponseEntity<Map<String, String>> response = agentService.updateTicketStatus(requestData);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Ticket status updated successfully", response.getBody().get("message"));
    }

    @Test
    void testAgentUpdateTicketStatusMissingData() {
        TicketRestService.AgentTicketRestService agentService = new TicketRestService.AgentTicketRestService();
        ReflectionTestUtils.setField(agentService, "ticketController", ticketController);
        
        Map<String, Object> requestData = new HashMap<>();
        
        when(ticketController.updateTicketStatus(requestData)).thenReturn(null);
        
        ResponseEntity<Map<String, String>> response = agentService.updateTicketStatus(requestData);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Ticket ID and status are required", response.getBody().get("message"));
    }

    @Test
    void testAgentUpdateTicketStatusNotFound() {
        TicketRestService.AgentTicketRestService agentService = new TicketRestService.AgentTicketRestService();
        ReflectionTestUtils.setField(agentService, "ticketController", ticketController);
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("ticketId", "1");
        requestData.put("status", "in_progress");
        
        when(ticketController.updateTicketStatus(requestData)).thenReturn(null);
        
        ResponseEntity<Map<String, String>> response = agentService.updateTicketStatus(requestData);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Ticket or agent not found", response.getBody().get("message"));
    }
}