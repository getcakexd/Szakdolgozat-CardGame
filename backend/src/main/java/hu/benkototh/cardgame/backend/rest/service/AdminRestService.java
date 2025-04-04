package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IGameRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    private UserRestService userRestService;

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
            User user = userOptional.get();
            userRestService.deleteUser(user.getId(), user.getPassword());
            //userRepository.delete(user);
            response.put("message", "User deleted successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "User not found.");
            return ResponseEntity.status(404).body(response);
        }
    }

    @GetMapping("/games/all")
    public ResponseEntity<List<Game>> getAllGames() {
        return ResponseEntity.ok(gameRepository.findAll());
    }

    @GetMapping("/games/get")
    public ResponseEntity<Game> getGame(@RequestParam long gameId) {
        Optional<Game> game = gameRepository.findById(gameId);

        if (game.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(game.get());
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

    @PutMapping("/games/update")
    public ResponseEntity<Map<String, String>> updateGame(@RequestBody Game game) {
        Optional<Game> gameOptional = gameRepository.findById(game.getId());
        Map<String, String> response = new HashMap<>();

        if (gameOptional.isEmpty()) {
            response.put("message", "Game not found.");
            return ResponseEntity.status(404).body(response);
        }

        Game existingGame = gameOptional.get();

        if (!existingGame.getName().equals(game.getName()) && gameExistsByName(game.getName())) {
            response.put("message", "Game with this name already exists.");
            return ResponseEntity.status(400).body(response);
        }

        existingGame.setName(game.getName());
        existingGame.setDescription(game.getDescription());
        existingGame.setActive(game.isActive());
        gameRepository.save(existingGame);

        response.put("message", "Game updated successfully.");
        return ResponseEntity.ok(response);
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