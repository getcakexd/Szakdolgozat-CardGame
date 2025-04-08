package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.AgentController;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/agent/users")
public class AgentRestService {

    @Autowired
    private AgentController agentController;
    @Autowired
    private UserController userController;

    @PutMapping("/unlock")
    public ResponseEntity<Map<String, String>> unlockUser(@RequestParam long userId) {
        Map<String, String> response = new HashMap<>();
        User user = agentController.unlockUser(userId);

        if (user == null) {
            response.put("message", "User not found.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "User unlocked successfully.");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/modify")
    public ResponseEntity<Map<String, String>> modifyUserData(@RequestBody Map<String, Object> userData) {
        Map<String, String> response = new HashMap<>();
        
        User user = agentController.modifyUserData(userData);
        
        if (user == null) {
            if (!userData.containsKey("userId")) {
                response.put("message", "User ID is required.");
                return ResponseEntity.status(400).body(response);
            }
            
            if (userData.containsKey("username") && userController.userExistsByUsername(userData.get("username").toString())) {
                response.put("message", "Username already in use.");
                return ResponseEntity.status(400).body(response);
            }
            
            if (userData.containsKey("email") && userController.userExistsByEmail(userData.get("email").toString())) {
                response.put("message", "Email already in use.");
                return ResponseEntity.status(400).body(response);
            }
            
            response.put("message", "User not found.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "User data modified successfully.");
        return ResponseEntity.ok(response);
    }
}
