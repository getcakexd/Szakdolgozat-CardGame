package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.AdminController;
import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminRestService {

    @Autowired
    private AdminController adminController;

    @GetMapping("/users/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminController.getAllUsers());
    }

    @PostMapping("/users/create")
    public ResponseEntity<Map<String, String>> createUser(@RequestBody User user) {
        Map<String, String> response = new HashMap<>();

        User createdUser = adminController.createUser(user);
        
        if (createdUser == null) {
            if (adminController.userExistsByUsername(user.getUsername())) {
                response.put("message", "Username already in use.");
                return ResponseEntity.status(400).body(response);
            } else if (adminController.userExistsByEmail(user.getEmail())) {
                response.put("message", "Email already in use.");
                return ResponseEntity.status(400).body(response);
            }
            return ResponseEntity.status(400).body(response);
        }

        response.put("message", "User created successfully.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/delete")
    public ResponseEntity<Map<String, String>> deleteUser(@RequestParam long userId) {
        Map<String, String> response = new HashMap<>();
        
        boolean deleted = adminController.deleteUser(userId);
        
        if (deleted) {
            response.put("message", "User deleted successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "User not found.");
            return ResponseEntity.status(404).body(response);
        }
    }

    @PutMapping("/users/promote-to-agent")
    public ResponseEntity<Map<String, String>> promoteToAgent(@RequestParam long userId) {
        Map<String, String> response = new HashMap<>();
        
        User user = adminController.promoteToAgent(userId);
        
        if (user == null) {
            response.put("message", "User not found or only regular users can be promoted to agents.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "User promoted to agent successfully.");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/demote-from-agent")
    public ResponseEntity<Map<String, String>> demoteFromAgent(@RequestParam long userId) {
        Map<String, String> response = new HashMap<>();
        
        User user = adminController.demoteFromAgent(userId);
        
        if (user == null) {
            response.put("message", "User not found or only agents can be demoted to users.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "User demoted from agent successfully.");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/promote-to-admin")
    public ResponseEntity<Map<String, String>> promoteToAdmin(@RequestParam long userId) {
        Map<String, String> response = new HashMap<>();
        
        User user = adminController.promoteToAdmin(userId);
        
        if (user == null) {
            response.put("message", "User not found or user already has admin or higher privileges.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "User promoted to admin successfully.");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/demote-from-admin")
    public ResponseEntity<Map<String, String>> demoteFromAdmin(@RequestParam long userId) {
        Map<String, String> response = new HashMap<>();
        
        User user = adminController.demoteFromAdmin(userId);
        
        if (user == null) {
            response.put("message", "User not found or only admins can be demoted.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "User demoted from admin successfully.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/games/all")
    public ResponseEntity<List<Game>> getAllGames() {
        return ResponseEntity.ok(adminController.getAllGames());
    }

    @PostMapping("/games/create")
    public ResponseEntity<Map<String, String>> createGame(@RequestBody Game game) {
        Map<String, String> response = new HashMap<>();
        
        Game createdGame = adminController.createGame(game);
        
        if (createdGame == null) {
            response.put("message", "Game with this name already exists.");
            return ResponseEntity.status(400).body(response);
        }
        
        response.put("message", "Game created successfully.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/games/delete")
    public ResponseEntity<Map<String, String>> deleteGame(@RequestParam long gameId) {
        Map<String, String> response = new HashMap<>();
        
        boolean deleted = adminController.deleteGame(gameId);
        
        if (deleted) {
            response.put("message", "Game deleted successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Game not found.");
            return ResponseEntity.status(404).body(response);
        }
    }
}
