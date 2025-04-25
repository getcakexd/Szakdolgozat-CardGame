package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.Ticket;
import hu.benkototh.cardgame.backend.rest.Data.TicketMessage;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.ITicketRepository;
import hu.benkototh.cardgame.backend.rest.repository.ITicketMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class TicketController {

    @Autowired
    private ITicketRepository ticketRepository;

    @Autowired
    private ITicketMessageRepository ticketMessageRepository;

    @Lazy
    @Autowired
    private UserController userController;

    @Autowired
    private AuditLogController auditLogController;

    public Ticket createTicket(Ticket ticket) {
        ticket.setStatus("new");

        if (ticket.getUserId() != null && ticket.getUserId() > 0) {
            User user = userController.getUser(ticket.getUserId());
            if (user != null) {
                ticket.setUser(user);
                ticket.setUserId(ticket.getUserId());
            }
        }

        Ticket savedTicket = ticketRepository.save(ticket);

        auditLogController.logAction("TICKET_CREATED", ticket.getUserId() != null ? ticket.getUserId() : 0L,
                "Ticket created with reference: " + savedTicket.getReference());

        return savedTicket;
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public List<Ticket> getTicketsByStatus(String status) {
        return ticketRepository.findAll().stream()
                .filter(ticket -> ticket.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    public List<Ticket> getUserTickets(Long userId) {
        User user = userController.getUser(userId);
        if (user == null) {
            return List.of();
        }

        return ticketRepository.findAll().stream()
                .filter(ticket -> ticket.getUser() != null && ticket.getUser().getId() == userId)
                .collect(Collectors.toList());
    }

    public Ticket getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId).orElse(null);
    }

    public Ticket getTicketByReference(String reference) {
        return ticketRepository.findAll().stream()
                .filter(ticket -> ticket.getReference().equals(reference))
                .findFirst()
                .orElse(null);
    }

    public List<TicketMessage> getTicketMessages(Long ticketId) {
        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);
        if (ticketOptional.isEmpty()) {
            return List.of();
        }

        Ticket ticket = ticketOptional.get();

        return ticketMessageRepository.findAll().stream()
                .filter(message -> message.getTicket().getId() == ticket.getId())
                .sorted((m1, m2) -> m1.getCreatedAt().compareTo(m2.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public TicketMessage createAndAddTicketMessage(
            Long ticketId,
            String messageContent,
            String senderName,
            String senderEmail,
            String senderType,
            Long userId) {

        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);
        if (ticketOptional.isEmpty()) {
            return null;
        }

        Ticket ticket = ticketOptional.get();

        TicketMessage message = new TicketMessage();
        message.setTicket(ticket);
        message.setTicketId(ticketId);
        message.setMessage(messageContent);
        message.setSenderName(senderName);

        if (senderEmail != null && !senderEmail.isEmpty()) {
            message.setSenderEmail(senderEmail);
        } else if (ticket.getEmail() != null) {
            message.setSenderEmail(ticket.getEmail());
        }

        if (senderType != null && !senderType.isEmpty()) {
            message.setSenderType(senderType);
            message.setFromAgent(senderType.equals("agent"));
        } else {
            message.setSenderType("user");
            message.setFromAgent(false);
        }

        if (userId != null && userId > 0) {
            User user = userController.getUser(userId);
            if (user != null) {
                message.setUser(user);
                message.setUserId(userId);
            }
        }

        message.setCreatedAt(new Date());

        ticket.setUpdatedAt(new Date());
        ticketRepository.save(ticket);

        TicketMessage savedMessage = ticketMessageRepository.save(message);

        auditLogController.logAction("TICKET_MESSAGE_ADDED", userId != null ? userId : 0L,
                "Message added to ticket: " + ticket.getReference());

        return savedMessage;
    }

    public TicketMessage addTicketMessage(Long ticketId, TicketMessage message, Long userId) {
        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);
        if (ticketOptional.isEmpty()) {
            return null;
        }

        Ticket ticket = ticketOptional.get();
        message.setTicket(ticket);
        message.setTicketId(ticketId);

        if (userId != null && userId > 0) {
            User user = userController.getUser(userId);
            if (user != null) {
                message.setUser(user);
                message.setUserId(userId);
                message.setSenderName(user.getUsername());
                message.setSenderEmail(user.getEmail());
                message.setFromAgent(user.getRole().equals("AGENT") || user.getRole().equals("ADMIN"));
            }
        }

        ticket.setUpdatedAt(new Date());
        ticketRepository.save(ticket);

        TicketMessage savedMessage = ticketMessageRepository.save(message);

        auditLogController.logAction("TICKET_MESSAGE_ADDED", userId != null ? userId : 0L,
                "Message added to ticket: " + ticket.getReference());

        return savedMessage;
    }

    public Ticket updateTicketStatus(Map<String, Object> requestData) {
        if (!requestData.containsKey("ticketId") || !requestData.containsKey("status")) {
            return null;
        }

        long ticketId = Long.parseLong(requestData.get("ticketId").toString());
        String status = requestData.get("status").toString();
        Long agentId = null;

        if (requestData.containsKey("agentId")) {
            agentId = Long.parseLong(requestData.get("agentId").toString());
        }

        Optional<Ticket> ticketOptional = ticketRepository.findById(ticketId);
        User agent = null;

        if (agentId != null) {
            agent = userController.getUser(agentId);
            if (agent == null) {
                return null;
            }
        }

        if (ticketOptional.isEmpty()) {
            return null;
        }

        Ticket ticket = ticketOptional.get();
        ticket.setStatus(status);

        if (agent != null) {
            ticket.setAssignedTo(agent);
            ticket.setAssignedToId(agentId);
        }

        Ticket updatedTicket = ticketRepository.save(ticket);

        auditLogController.logAction("TICKET_STATUS_UPDATED", agentId != null ? agentId : 0L,
                "Ticket " + ticket.getReference() + " status updated to: " + status);

        return updatedTicket;
    }

    public List<Ticket> getAgentTickets(Long agentId) {
        User agent = userController.getUser(agentId);
        if (agent == null) {
            return List.of();
        }

        return ticketRepository.findAll().stream()
                .filter(ticket -> ticket.getAssignedTo() != null && ticket.getAssignedTo().getId() == agentId)
                .collect(Collectors.toList());
    }

    public List<Ticket> getTicketsByEmail(String email) {
        return ticketRepository.findAll().stream()
                .filter(ticket -> ticket.getEmail().equals(email))
                .collect(Collectors.toList());
    }

    public List<Ticket> getTicketsByCategory(String category) {
        return ticketRepository.findAll().stream()
                .filter(ticket -> ticket.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    public List<Ticket> getTicketsByDateRange(Date startDate, Date endDate) {
        return ticketRepository.findAll().stream()
                .filter(ticket -> ticket.getCreatedAt().after(startDate) && ticket.getCreatedAt().before(endDate))
                .collect(Collectors.toList());
    }

}