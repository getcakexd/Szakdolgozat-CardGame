package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.ContactRequest;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IContactRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ContactController {

    @Autowired
    private IContactRequestRepository contactRequestRepository;
    
    @Lazy
    @Autowired
    private UserController userController;
    
    @Autowired
    private AuditLogController auditLogController;

    public ContactRequest submitContactRequest(ContactRequest request) {
        request.setStatus("new");
        ContactRequest savedRequest = contactRequestRepository.save(request);

        auditLogController.logAction("CONTACT_REQUEST_SUBMITTED", 0L,
                "Contact request submitted with subject: " + request.getSubject());
        
        return savedRequest;
    }
    
    public List<ContactRequest> getAllContactRequests() {
        return contactRequestRepository.findAll();
    }
    
    public ContactRequest getContactRequest(long requestId) {

        return contactRequestRepository.findById(requestId).orElse(null);
    }
    
    public ContactRequest updateContactRequestStatus(Map<String, Object> requestData) {
        if (!requestData.containsKey("requestId") || !requestData.containsKey("status") || !requestData.containsKey("agentId")) {
            return null;
        }

        long requestId = Long.parseLong(requestData.get("requestId").toString());
        String status = requestData.get("status").toString();
        long agentId = Long.parseLong(requestData.get("agentId").toString());

        Optional<ContactRequest> requestOptional = contactRequestRepository.findById(requestId);
        User agent = userController.getUser(agentId);

        if (requestOptional.isEmpty() || agent == null) {
            return null;
        }

        ContactRequest contactRequest = requestOptional.get();
        contactRequest.setStatus(status);
        contactRequest.setAssignedTo(agent);
        ContactRequest updatedRequest = contactRequestRepository.save(contactRequest);
        
        auditLogController.logAction("CONTACT_REQUEST_STATUS_UPDATED", agentId,
                "Contact request " + requestId + " status updated to: " + status);
        
        return updatedRequest;
    }
}
