package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.Data.UserHistory;
import hu.benkototh.cardgame.backend.rest.repository.IGameRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserHistoryRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminRestService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IGameRepository gameRepository;

    @Autowired
    private IUserHistoryRepository userHistoryRepository;

    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("/users/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/users/create")
    public ResponseEntity<Map<String, String>> createUser(@RequestBody User user) {
        Map<String, String> response = new HashMap<>();

        if (userExistsByUsername(user.getUsername())) {
            response.put("message", "Username already in use.");
            return ResponseEntity.status(400).body(response);
        } else if (userExistsByEmail(user.getEmail())) {
            response.put("message", "Email already in use.");
            return ResponseEntity.status(400).body(response);
        } else {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            user.setLocked(false);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);

            response.put("message", "User created successfully.");
            return ResponseEntity.ok(response);
        }
    }

    @DeleteMapping("/users/delete")
    public ResponseEntity<Map<String, String>> deleteUser(@RequestParam long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        Map<String, String> response = new HashMap<>();

        if (userOptional.isPresent()) {
            userRepository.delete(userOptional.get());
            response.put("message", "User deleted successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "User not found.");
            return ResponseEntity.status(404).body(response);
        }
    }

    @PutMapping("/users/promote-to-agent")
    public ResponseEntity<Map<String, String>> promoteToAgent(@RequestParam long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        Map<String, String> response = new HashMap<>();

        if (userOptional.isEmpty()) {
            response.put("message", "User not found.");
            return ResponseEntity.status(404).body(response);
        }

        User user = userOptional.get();

        if (!user.getRole().equals("ROLE_USER")) {
            response.put("message", "Only regular users can be promoted to agents.");
            return ResponseEntity.status(400).body(response);
        }

        user.setRole("ROLE_AGENT");
        userRepository.save(user);

        response.put("message", "User promoted to agent successfully.");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/demote-from-agent")
    public ResponseEntity<Map<String, String>> demoteFromAgent(@RequestParam long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        Map<String, String> response = new HashMap<>();

        if (userOptional.isEmpty()) {
            response.put("message", "User not found.");
            return ResponseEntity.status(404).body(response);
        }

        User user = userOptional.get();

        if (!user.getRole().equals("ROLE_AGENT")) {
            response.put("message", "Only agents can be demoted to users.");
            return ResponseEntity.status(400).body(response);
        }

        user.setRole("ROLE_USER");
        userRepository.save(user);

        response.put("message", "User demoted from agent successfully.");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/promote-to-admin")
    public ResponseEntity<Map<String, String>> promoteToAdmin(@RequestParam long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        Map<String, String> response = new HashMap<>();

        if (userOptional.isEmpty()) {
            response.put("message", "User not found.");
            return ResponseEntity.status(404).body(response);
        }

        User user = userOptional.get();

        if (user.getRole().equals("ROLE_ADMIN") || user.getRole().equals("ROLE_ROOT")) {
            response.put("message", "User already has admin or higher privileges.");
            return ResponseEntity.status(400).body(response);
        }

        user.setRole("ROLE_ADMIN");
        userRepository.save(user);

        response.put("message", "User promoted to admin successfully.");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/users/demote-from-admin")
    public ResponseEntity<Map<String, String>> demoteFromAdmin(@RequestParam long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        Map<String, String> response = new HashMap<>();

        if (userOptional.isEmpty()) {
            response.put("message", "User not found.");
            return ResponseEntity.status(404).body(response);
        }

        User user = userOptional.get();

        if (!user.getRole().equals("ROLE_ADMIN")) {
            response.put("message", "Only admins can be demoted.");
            return ResponseEntity.status(400).body(response);
        }

        user.setRole("ROLE_AGENT");
        userRepository.save(user);

        response.put("message", "User demoted from admin successfully.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/games/all")
    public ResponseEntity<List<Game>> getAllGames() {
        return ResponseEntity.ok(gameRepository.findAll());
    }

    @PostMapping("/games/create")
    public ResponseEntity<Map<String, String>> createGame(@RequestBody Game game) {
        Map<String, String> response = new HashMap<>();

        if (gameExistsByName(game.getName())) {
            response.put("message", "Game with this name already exists.");
            return ResponseEntity.status(400).body(response);
        } else {
            gameRepository.save(game);
            response.put("message", "Game created successfully.");
            return ResponseEntity.ok(response);
        }
    }

    @DeleteMapping("/games/delete")
    public ResponseEntity<Map<String, String>> deleteGame(@RequestParam long gameId) {
        Optional<Game> gameOptional = gameRepository.findById(gameId);
        Map<String, String> response = new HashMap<>();

        if (gameOptional.isEmpty()) {
            response.put("message", "Game not found.");
            return ResponseEntity.status(404).body(response);
        }

        gameRepository.delete(gameOptional.get());
        response.put("message", "Game deleted successfully.");
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

    private boolean gameExistsByName(String name) {
        return gameRepository.findAll().stream()
                .anyMatch(game -> game.getName().equals(name));
    }
}

