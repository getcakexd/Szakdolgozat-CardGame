package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Ticket;
import hu.benkototh.cardgame.backend.rest.Data.TicketMessage;
import hu.benkototh.cardgame.backend.rest.controller.TicketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
public class TicketRestService {

    @Autowired
    private TicketController ticketController;

    @PostMapping("/create")
    public ResponseEntity<Ticket> createTicket(@RequestBody Ticket ticket) {
        Ticket createdTicket = ticketController.createTicket(ticket);

        return ResponseEntity.ok(createdTicket);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getTicketById(@PathVariable Long id) {
        Ticket ticket = ticketController.getTicketById(id);

        if (ticket == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Ticket not found");
            return ResponseEntity.status(404).body(response);
        }

        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<?> getTicketByReference(@PathVariable String reference) {
        Ticket ticket = ticketController.getTicketByReference(reference);

        if (ticket == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Ticket not found");
            return ResponseEntity.status(404).body(response);
        }

        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Ticket>> getUserTickets(@PathVariable Long userId) {
        return ResponseEntity.ok(ticketController.getUserTickets(userId));
    }

    @GetMapping("/messages/{ticketId}")
    public ResponseEntity<?> getTicketMessagesById(@PathVariable Long ticketId) {
        Ticket ticket = ticketController.getTicketById(ticketId);

        if (ticket == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Ticket not found");
            return ResponseEntity.status(404).body(response);
        }

        List<TicketMessage> messages = ticketController.getTicketMessages(ticketId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/messages/add")
    public ResponseEntity<?> addTicketMessage(
            @RequestParam Long ticketId,
            @RequestParam String message,
            @RequestParam String senderName,
            @RequestParam(required = false) String senderEmail,
            @RequestParam(required = false) String senderType,
            @RequestParam(required = false) Long userId) {

        Ticket ticket = ticketController.getTicketById(ticketId);

        if (ticket == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Ticket not found with ID: " + ticketId);
            return ResponseEntity.status(404).body(response);
        }

        try {
            TicketMessage savedMessage = ticketController.createAndAddTicketMessage(
                    ticketId, message, senderName, senderEmail, senderType, userId);

            if (savedMessage == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Failed to add message");
                return ResponseEntity.status(400).body(response);
            }

            return ResponseEntity.ok(savedMessage);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error processing request: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/{ticketId}/messages")
    public ResponseEntity<?> getTicketMessages(@PathVariable Long ticketId) {
        Ticket ticket = ticketController.getTicketById(ticketId);

        if (ticket == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Ticket not found");
            return ResponseEntity.status(404).body(response);
        }

        List<TicketMessage> messages = ticketController.getTicketMessages(ticketId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{ticketId}/messages")
    public ResponseEntity<?> addTicketMessage(
            @PathVariable Long ticketId,
            @RequestBody TicketMessage message,
            @RequestParam(required = false) Long userId) {

        Ticket ticket = ticketController.getTicketById(ticketId);

        if (ticket == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Ticket not found");
            return ResponseEntity.status(404).body(response);
        }

        TicketMessage savedMessage = ticketController.addTicketMessage(ticketId, message, userId);

        if (savedMessage == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to add message");
            return ResponseEntity.status(400).body(response);
        }

        return ResponseEntity.ok(savedMessage);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<List<Ticket>> getTicketsByEmail(@PathVariable String email) {
        return ResponseEntity.ok(ticketController.getTicketsByEmail(email));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Ticket>> getTicketsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(ticketController.getTicketsByCategory(category));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Ticket>> getTicketsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        return ResponseEntity.ok(ticketController.getTicketsByDateRange(startDate, endDate));
    }

    @RestController
    @RequestMapping("/api/agent/tickets")
    public static class AgentTicketRestService {

        @Autowired
        private TicketController ticketController;

        @GetMapping("/all")
        public ResponseEntity<List<Ticket>> getAllTickets() {
            return ResponseEntity.ok(ticketController.getAllTickets());
        }

        @GetMapping("/status/{status}")
        public ResponseEntity<List<Ticket>> getTicketsByStatus(@PathVariable String status) {
            return ResponseEntity.ok(ticketController.getTicketsByStatus(status));
        }

        @GetMapping("/assigned/{agentId}")
        public ResponseEntity<List<Ticket>> getAgentTickets(@PathVariable Long agentId) {
            return ResponseEntity.ok(ticketController.getAgentTickets(agentId));
        }

        @GetMapping("/{ticketId}")
        public ResponseEntity<?> getTicket(@PathVariable Long ticketId) {
            Ticket ticket = ticketController.getTicketById(ticketId);

            if (ticket == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Ticket not found");
                return ResponseEntity.status(404).body(response);
            }

            return ResponseEntity.ok(ticket);
        }

        @PutMapping("/update-status")
        public ResponseEntity<Map<String, String>> updateTicketStatus(@RequestBody Map<String, Object> requestData) {
            Map<String, String> response = new HashMap<>();

            Ticket updatedTicket = ticketController.updateTicketStatus(requestData);

            if (updatedTicket == null) {
                if (!requestData.containsKey("ticketId") || !requestData.containsKey("status")) {
                    response.put("message", "Ticket ID and status are required");
                    return ResponseEntity.status(400).body(response);
                }

                response.put("message", "Ticket or agent not found");
                return ResponseEntity.status(404).body(response);
            }

            response.put("message", "Ticket status updated successfully");
            return ResponseEntity.ok(response);
        }
    }
}