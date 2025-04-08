package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.UserController;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.Data.UserHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserRestService {

    @Autowired
    private UserController userController;

    @GetMapping("all")
    public ResponseEntity<List<User>> all() {
        return ResponseEntity.ok(userController.getAllUsers());
    }

    @GetMapping("/get")
    public ResponseEntity<User> getUser(@RequestParam long userId) {
        User user = userController.getUser(userId);

        if (user == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        User loggedInUser = userController.login(user);

        if (loggedInUser == null) {
            User existingUser = userController.findByUsername(user.getUsername());
            
            if (existingUser == null) {
                return ResponseEntity.status(401).body("Invalid username or password");
            }
            
            if (existingUser.isLocked()) {
                return ResponseEntity.status(401).body("Account is locked. Please contact support.");
            }
            
            return ResponseEntity.status(401).body("Invalid username or password");
        }

        return ResponseEntity.ok(loggedInUser);
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> create(@RequestBody User user) {
        Map<String, String> response = new HashMap<>();
        User createdUser = userController.createUser(user);
        
        if (createdUser == null) {
            if (userController.userExistsByUsername(user.getUsername())) {
                response.put("message", "Username already in use.");
            } else if (userController.userExistsByEmail(user.getEmail())) {
                response.put("message", "Email already in use.");
            } else {
                response.put("message", "Failed to create user.");
            }
            return ResponseEntity.status(400).body(response);
        }
        
        response.put("message", "User created.");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/username")
    public ResponseEntity<Map<String, String>> updateUsername(@RequestParam long userId, @RequestParam String newUsername) {
        Map<String, String> response = new HashMap<>();
        User updatedUser = userController.updateUsername(userId, newUsername);
        
        if (updatedUser == null) {
            if (userController.getUser(userId) == null) {
                response.put("message", "User not found.");
                return ResponseEntity.status(404).body(response);
            } else {
                response.put("message", "Username already in use.");
                return ResponseEntity.status(400).body(response);
            }
        }
        
        response.put("message", "Username updated.");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/email")
    public ResponseEntity<Map<String, String>> updateEmail(@RequestParam long userId, @RequestParam String newEmail) {
        Map<String, String> response = new HashMap<>();
        User updatedUser = userController.updateEmail(userId, newEmail);
        
        if (updatedUser == null) {
            if (userController.getUser(userId) == null) {
                response.put("message", "User not found.");
                return ResponseEntity.status(404).body(response);
            } else {
                response.put("message", "Email already in use.");
                return ResponseEntity.status(400).body(response);
            }
        }
        
        response.put("message", "Email updated.");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/password")
    public ResponseEntity<Map<String, String>> updatePassword(@RequestParam long userId, @RequestParam String currentPassword, @RequestParam String newPassword) {
        Map<String, String> response = new HashMap<>();
        User updatedUser = userController.updatePassword(userId, currentPassword, newPassword);
        
        if (updatedUser == null) {
            User user = userController.getUser(userId);
            
            if (user == null) {
                response.put("message", "User not found.");
                return ResponseEntity.status(404).body(response);
            } else if (userController.passwordEncoder.matches(newPassword, user.getPassword())) {
                response.put("message", "New password cannot be the same as the old one.");
                return ResponseEntity.status(400).body(response);
            } else {
                response.put("message", "Incorrect password.");
                return ResponseEntity.status(400).body(response);
            }
        }
        
        response.put("message", "Password updated.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteUser(@RequestParam long userId, @RequestParam String password) {
        Map<String, String> response = new HashMap<>();
        boolean deleted = userController.deleteUser(userId, password);
        
        if (!deleted) {
            User user = userController.getUser(userId);
            
            if (user == null) {
                response.put("message", "User not found.");
                return ResponseEntity.status(404).body(response);
            } else {
                response.put("message", "Incorrect password.");
                return ResponseEntity.status(400).body(response);
            }
        }
        
        response.put("message", "User deleted.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<UserHistory>> getUserHistory(@RequestParam long userId) {
        List<UserHistory> history = userController.getUserHistory(userId);
        
        if (history == null) {
            return ResponseEntity.status(404).body(null);
        }
        
        return ResponseEntity.ok(history);
    }
}
