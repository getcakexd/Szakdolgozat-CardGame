package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.ContactRequest;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IContactRequestRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/contact")
public class ContactRestService {

    @Autowired
    private IContactRequestRepository contactRequestRepository;

    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submitContactRequest(@RequestBody ContactRequest request) {
        Map<String, String> response = new HashMap<>();

        request.setStatus("new");
        contactRequestRepository.save(request);

        response.put("message", "Contact request submitted successfully.");
        return ResponseEntity.ok(response);
    }

    @RestController
    @RequestMapping("/api/agent/contact")
    public static class AgentContactRestService {

        @Autowired
        private IContactRequestRepository contactRequestRepository;

        @Autowired
        private IUserRepository userRepository;

        @GetMapping("/all")
        public ResponseEntity<List<ContactRequest>> getAllContactRequests() {
            return ResponseEntity.ok(contactRequestRepository.findAll());
        }

        @GetMapping("/get")
        public ResponseEntity<ContactRequest> getContactRequest(@RequestParam long requestId) {
            Optional<ContactRequest> request = contactRequestRepository.findById(requestId);

            if (request.isEmpty()) {
                return ResponseEntity.status(404).body(null);
            }

            return ResponseEntity.ok(request.get());
        }

        @PutMapping("/update-status")
        public ResponseEntity<Map<String, String>> updateContactRequestStatus(@RequestBody Map<String, Object> requestData) {
            Map<String, String> response = new HashMap<>();

            if (!requestData.containsKey("requestId") || !requestData.containsKey("status") || !requestData.containsKey("agentId")) {
                response.put("message", "Request ID, status, and agent ID are required.");
                return ResponseEntity.status(400).body(response);
            }

            long requestId = Long.parseLong(requestData.get("requestId").toString());
            String status = requestData.get("status").toString();
            long agentId = Long.parseLong(requestData.get("agentId").toString());

            Optional<ContactRequest> requestOptional = contactRequestRepository.findById(requestId);
            Optional<User> agentOptional = userRepository.findById(agentId);

            if (requestOptional.isEmpty()) {
                response.put("message", "Contact request not found.");
                return ResponseEntity.status(404).body(response);
            }

            if (agentOptional.isEmpty()) {
                response.put("message", "Agent not found.");
                return ResponseEntity.status(404).body(response);
            }

            ContactRequest contactRequest = requestOptional.get();
            User agent = agentOptional.get();

            contactRequest.setStatus(status);
            contactRequest.setAssignedTo(agent);
            contactRequestRepository.save(contactRequest);

            response.put("message", "Contact request status updated successfully.");
            return ResponseEntity.ok(response);
        }
    }
}