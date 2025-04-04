package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.Data.UserHistory;
import hu.benkototh.cardgame.backend.rest.repository.IUserHistoryRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/agent/users")
public class AgentRestService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IUserHistoryRepository userHistoryRepository;

    @PutMapping("/unlock")
    public ResponseEntity<Map<String, String>> unlockUser(@RequestParam long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        Map<String, String> response = new HashMap<>();

        if (userOptional.isEmpty()) {
            response.put("message", "User not found.");
            return ResponseEntity.status(404).body(response);
        }

        User user = userOptional.get();
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        response.put("message", "User unlocked successfully.");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/modify")
    public ResponseEntity<Map<String, String>> modifyUserData(@RequestBody Map<String, Object> userData) {
        Map<String, String> response = new HashMap<>();

        if (!userData.containsKey("userId")) {
            response.put("message", "User ID is required.");
            return ResponseEntity.status(400).body(response);
        }

        long userId = Long.parseLong(userData.get("userId").toString());
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            response.put("message", "User not found.");
            return ResponseEntity.status(404).body(response);
        }

        User user = userOptional.get();
        String previousUsername = null;
        String previousEmail = null;
        boolean modified = false;

        if (userData.containsKey("username") && !userData.get("username").equals(user.getUsername())) {
            String newUsername = userData.get("username").toString();

            if (userExistsByUsername(newUsername)) {
                response.put("message", "Username already in use.");
                return ResponseEntity.status(400).body(response);
            }

            previousUsername = user.getUsername();
            user.setUsername(newUsername);
            modified = true;
        }

        if (userData.containsKey("email") && !userData.get("email").equals(user.getEmail())) {
            String newEmail = userData.get("email").toString();

            if (userExistsByEmail(newEmail)) {
                response.put("message", "Email already in use.");
                return ResponseEntity.status(400).body(response);
            }

            previousEmail = user.getEmail();
            user.setEmail(newEmail);
            modified = true;
        }

        if (modified) {
            userRepository.save(user);

            UserHistory history = new UserHistory();
            history.setUser(user);
            history.setPreviousUsername(previousUsername);
            history.setPreviousEmail(previousEmail);
            history.setChangedAt(new Date());
            history.setChangedBy("agent");
            userHistoryRepository.save(history);
        }

        response.put("message", "User data modified successfully.");
        return ResponseEntity.ok(response);
    }

    private boolean userExistsByUsername(String username) {
        return userRepository.findAll().stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }

    private boolean userExistsByEmail(String email) {
        return userRepository.findAll().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }
}