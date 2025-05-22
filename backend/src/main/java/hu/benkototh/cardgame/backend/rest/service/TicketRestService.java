package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.model.Ticket;
import hu.benkototh.cardgame.backend.rest.model.TicketMessage;
import hu.benkototh.cardgame.backend.rest.controller.TicketController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Support Tickets", description = "Operations for managing support tickets")
public class TicketRestService {

    @Autowired
    private TicketController ticketController;

    @Operation(summary = "Create ticket", description = "Creates a new support ticket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Ticket.class)))
    })
    @PostMapping("/create")
    public ResponseEntity<Ticket> createTicket(
            @Parameter(description = "Ticket details", required = true) @RequestBody Ticket ticket) {
        Ticket createdTicket = ticketController.createTicket(ticket);

        return ResponseEntity.ok(createdTicket);
    }

    @Operation(summary = "Get ticket by ID", description = "Retrieves a specific ticket by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved ticket",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Ticket.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @GetMapping("/get/{id}")
    public ResponseEntity<?> getTicketById(
            @Parameter(description = "Ticket ID", required = true) @PathVariable Long id) {
        Ticket ticket = ticketController.getTicketById(id);

        if (ticket == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Ticket not found");
            return ResponseEntity.status(404).body(response);
        }

        return ResponseEntity.ok(ticket);
    }

    @Operation(summary = "Get ticket by reference", description = "Retrieves a specific ticket by its reference number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved ticket",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Ticket.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @GetMapping("/reference/{reference}")
    public ResponseEntity<?> getTicketByReference(
            @Parameter(description = "Ticket reference number", required = true) @PathVariable String reference) {
        Ticket ticket = ticketController.getTicketByReference(reference);

        if (ticket == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Ticket not found");
            return ResponseEntity.status(404).body(response);
        }

        return ResponseEntity.ok(ticket);
    }

    @Operation(summary = "Get user tickets", description = "Retrieves all tickets created by a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user tickets",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Ticket.class)))
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Ticket>> getUserTickets(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        return ResponseEntity.ok(ticketController.getUserTickets(userId));
    }

    @Operation(summary = "Get ticket messages", description = "Retrieves all messages for a specific ticket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved ticket messages",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketMessage.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @GetMapping("/messages/{ticketId}")
    public ResponseEntity<?> getTicketMessagesById(
            @Parameter(description = "Ticket ID", required = true) @PathVariable Long ticketId) {
        Ticket ticket = ticketController.getTicketById(ticketId);

        if (ticket == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Ticket not found");
            return ResponseEntity.status(404).body(response);
        }

        List<TicketMessage> messages = ticketController.getTicketMessages(ticketId);
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "Add ticket message", description = "Adds a new message to an existing ticket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message added successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketMessage.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found"),
            @ApiResponse(responseCode = "400", description = "Failed to add message"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @PostMapping("/messages/add")
    public ResponseEntity<?> addTicketMessage(
            @Parameter(description = "Ticket ID", required = true) @RequestParam Long ticketId,
            @Parameter(description = "Message content", required = true) @RequestParam String message,
            @Parameter(description = "Sender name", required = true) @RequestParam String senderName,
            @Parameter(description = "Sender email", required = false) @RequestParam(required = false) String senderEmail,
            @Parameter(description = "Sender type (USER, AGENT, ADMIN)", required = false) @RequestParam(required = false) String senderType,
            @Parameter(description = "User ID (if authenticated)", required = false) @RequestParam(required = false) Long userId) {

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

    @Operation(summary = "Get ticket messages (alternative)", description = "Alternative endpoint to retrieve all messages for a specific ticket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved ticket messages",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketMessage.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @GetMapping("/{ticketId}/messages")
    public ResponseEntity<?> getTicketMessages(
            @Parameter(description = "Ticket ID", required = true) @PathVariable Long ticketId) {
        Ticket ticket = ticketController.getTicketById(ticketId);

        if (ticket == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Ticket not found");
            return ResponseEntity.status(404).body(response);
        }

        List<TicketMessage> messages = ticketController.getTicketMessages(ticketId);
        return ResponseEntity.ok(messages);
    }

    @Operation(summary = "Add ticket message (alternative)", description = "Alternative endpoint to add a message to a ticket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message added successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TicketMessage.class))),
            @ApiResponse(responseCode = "404", description = "Ticket not found"),
            @ApiResponse(responseCode = "400", description = "Failed to add message")
    })
    @PostMapping("/{ticketId}/messages")
    public ResponseEntity<?> addTicketMessage(
            @Parameter(description = "Ticket ID", required = true) @PathVariable Long ticketId,
            @Parameter(description = "Message details", required = true) @RequestBody TicketMessage message,
            @Parameter(description = "User ID (if authenticated)", required = false) @RequestParam(required = false) Long userId) {

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

    @Operation(summary = "Get tickets by email", description = "Retrieves all tickets associated with a specific email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tickets",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Ticket.class)))
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<List<Ticket>> getTicketsByEmail(
            @Parameter(description = "Email address", required = true) @PathVariable String email) {
        return ResponseEntity.ok(ticketController.getTicketsByEmail(email));
    }

    @Operation(summary = "Get tickets by category", description = "Retrieves all tickets in a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tickets",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Ticket.class)))
    })
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Ticket>> getTicketsByCategory(
            @Parameter(description = "Ticket category", required = true) @PathVariable String category) {
        return ResponseEntity.ok(ticketController.getTicketsByCategory(category));
    }

    @Operation(summary = "Get tickets by date range", description = "Retrieves all tickets created within a specific date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tickets",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Ticket.class)))
    })
    @GetMapping("/date-range")
    public ResponseEntity<List<Ticket>> getTicketsByDateRange(
            @Parameter(description = "Start date (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @Parameter(description = "End date (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        return ResponseEntity.ok(ticketController.getTicketsByDateRange(startDate, endDate));
    }

    @RestController
    @RequestMapping("/api/agent/tickets")
    @Tag(name = "Agent Ticket Operations", description = "Operations for agents to manage support tickets")
    public static class AgentTicketRestService {

        @Autowired
        private TicketController ticketController;

        @Operation(summary = "Get all tickets", description = "Retrieves all tickets in the system (agent access)")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Successfully retrieved tickets",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = Ticket.class)))
        })
        @GetMapping("/all")
        public ResponseEntity<List<Ticket>> getAllTickets() {
            return ResponseEntity.ok(ticketController.getAllTickets());
        }

        @Operation(summary = "Get tickets by status", description = "Retrieves all tickets with a specific status")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Successfully retrieved tickets",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = Ticket.class)))
        })
        @GetMapping("/status/{status}")
        public ResponseEntity<List<Ticket>> getTicketsByStatus(
                @Parameter(description = "Ticket status", required = true) @PathVariable String status) {
            return ResponseEntity.ok(ticketController.getTicketsByStatus(status));
        }

        @Operation(summary = "Get tickets assigned to agent", description = "Retrieves all tickets assigned to a specific agent")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Successfully retrieved tickets",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = Ticket.class)))
        })
        @GetMapping("/assigned/{agentId}")
        public ResponseEntity<List<Ticket>> getAgentTickets(
                @Parameter(description = "Agent user ID", required = true) @PathVariable Long agentId) {
            return ResponseEntity.ok(ticketController.getAgentTickets(agentId));
        }

        @Operation(summary = "Get ticket details", description = "Retrieves detailed information about a specific ticket")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Successfully retrieved ticket",
                        content = @Content(mediaType = "application/json", schema = @Schema(implementation = Ticket.class))),
                @ApiResponse(responseCode = "404", description = "Ticket not found")
        })
        @GetMapping("/{ticketId}")
        public ResponseEntity<?> getTicket(
                @Parameter(description = "Ticket ID", required = true) @PathVariable Long ticketId) {
            Ticket ticket = ticketController.getTicketById(ticketId);

            if (ticket == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Ticket not found");
                return ResponseEntity.status(404).body(response);
            }

            return ResponseEntity.ok(ticket);
        }

        @Operation(summary = "Update ticket status", description = "Updates the status of a ticket")
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Ticket status updated successfully"),
                @ApiResponse(responseCode = "400", description = "Invalid request - missing required fields"),
                @ApiResponse(responseCode = "404", description = "Ticket or agent not found")
        })
        @PutMapping("/update-status")
        public ResponseEntity<Map<String, String>> updateTicketStatus(
                @Parameter(description = "Status update details", required = true) @RequestBody Map<String, Object> requestData) {
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