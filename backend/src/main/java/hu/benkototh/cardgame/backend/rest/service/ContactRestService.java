package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.ContactController;
import hu.benkototh.cardgame.backend.rest.Data.ContactRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contact")
public class ContactRestService {

    @Autowired
    private ContactController contactController;

    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submitContactRequest(@RequestBody ContactRequest request) {
        Map<String, String> response = new HashMap<>();

        contactController.submitContactRequest(request);

        response.put("message", "Contact request submitted successfully.");
        return ResponseEntity.ok(response);
    }

    @RestController
    @RequestMapping("/api/agent/contact")
    public static class AgentContactRestService {

        @Autowired
        private ContactController contactController;

        @GetMapping("/all")
        public ResponseEntity<List<ContactRequest>> getAllContactRequests() {
            return ResponseEntity.ok(contactController.getAllContactRequests());
        }

        @GetMapping("/get")
        public ResponseEntity<ContactRequest> getContactRequest(@RequestParam long requestId) {
            ContactRequest request = contactController.getContactRequest(requestId);

            if (request == null) {
                return ResponseEntity.status(404).body(null);
            }

            return ResponseEntity.ok(request);
        }

        @PutMapping("/update-status")
        public ResponseEntity<Map<String, String>> updateContactRequestStatus(@RequestBody Map<String, Object> requestData) {
            Map<String, String> response = new HashMap<>();

            ContactRequest contactRequest = contactController.updateContactRequestStatus(requestData);
            
            if (contactRequest == null) {
                if (!requestData.containsKey("requestId") || !requestData.containsKey("status") || !requestData.containsKey("agentId")) {
                    response.put("message", "Request ID, status, and agent ID are required.");
                    return ResponseEntity.status(400).body(response);
                }
                
                response.put("message", "Contact request or agent not found.");
                return ResponseEntity.status(404).body(response);
            }

            response.put("message", "Contact request status updated successfully.");
            return ResponseEntity.ok(response);
        }
    }
}
