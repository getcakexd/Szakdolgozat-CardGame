package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IGameRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import hu.benkototh.cardgame.backend.rest.controller.AuditLogController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class AdminController {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IGameRepository gameRepository;

    @Autowired
    private AuditLogController auditLogController;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user) {
        if (userExistsByUsername(user.getUsername()) || userExistsByEmail(user.getEmail())) {
            auditLogController.logAction("ADMIN_USER_CREATION_FAILED", 0L,
                    "Admin user creation failed: Username or email already exists - " + user.getUsername());
            return null;
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        User createdUser = userRepository.save(user);

        auditLogController.logAction("ADMIN_CREATED_USER", 0L,
                "Admin created user: " + user.getUsername() + " with role: " + user.getRole() + " (ID: " + createdUser.getId() + ")");

        return createdUser;
    }

    public boolean deleteUser(long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            auditLogController.logAction("ADMIN_USER_DELETION_FAILED", 0L,
                    "Admin user deletion failed: User not found - ID: " + userId);
            return false;
        }

        User user = userOptional.get();
        userRepository.delete(user);

        auditLogController.logAction("ADMIN_DELETED_USER", 0L,
                "Admin deleted user: " + user.getUsername() + " (ID: " + userId + ")");

        return true;
    }

    public User promoteToAgent(long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            auditLogController.logAction("ADMIN_PROMOTION_FAILED", 0L,
                    "Admin promotion failed: User not found - ID: " + userId);
            return null;
        }

        User user = userOptional.get();

        if (!user.getRole().equals("ROLE_USER")) {
            auditLogController.logAction("ADMIN_PROMOTION_FAILED", 0L,
                    "Admin promotion failed: User is not a regular user - ID: " + userId);
            return null;
        }

        user.setRole("ROLE_AGENT");
        User updatedUser = userRepository.save(user);

        auditLogController.logAction("ADMIN_PROMOTED_TO_AGENT", 0L,
                "Admin promoted user to agent: " + user.getUsername() + " (ID: " + userId + ")");

        return updatedUser;
    }

    public User demoteFromAgent(long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            auditLogController.logAction("ADMIN_DEMOTION_FAILED", 0L,
                    "Admin demotion failed: User not found - ID: " + userId);
            return null;
        }

        User user = userOptional.get();

        if (!user.getRole().equals("ROLE_AGENT")) {
            auditLogController.logAction("ADMIN_DEMOTION_FAILED", 0L,
                    "Admin demotion failed: User is not an agent - ID: " + userId);
            return null;
        }

        user.setRole("ROLE_USER");
        User updatedUser = userRepository.save(user);

        auditLogController.logAction("ADMIN_DEMOTED_FROM_AGENT", 0L,
                "Admin demoted user from agent: " + user.getUsername() + " (ID: " + userId + ")");

        return updatedUser;
    }

    public User promoteToAdmin(long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            auditLogController.logAction("ADMIN_PROMOTION_FAILED", 0L,
                    "Admin promotion failed: User not found - ID: " + userId);
            return null;
        }

        User user = userOptional.get();

        if (user.getRole().equals("ROLE_ADMIN") || user.getRole().equals("ROLE_ROOT")) {
            auditLogController.logAction("ADMIN_PROMOTION_FAILED", 0L,
                    "Admin promotion failed: User is already admin or root - ID: " + userId);
            return null;
        }

        user.setRole("ROLE_ADMIN");
        User updatedUser = userRepository.save(user);

        auditLogController.logAction("ADMIN_PROMOTED_TO_ADMIN", 0L,
                "Admin promoted user to admin: " + user.getUsername() + " (ID: " + userId + ")");

        return updatedUser;
    }

    public User demoteFromAdmin(long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            auditLogController.logAction("ADMIN_DEMOTION_FAILED", 0L,
                    "Admin demotion failed: User not found - ID: " + userId);
            return null;
        }

        User user = userOptional.get();

        if (!user.getRole().equals("ROLE_ADMIN")) {
            auditLogController.logAction("ADMIN_DEMOTION_FAILED", 0L,
                    "Admin demotion failed: User is not an admin - ID: " + userId);
            return null;
        }

        user.setRole("ROLE_AGENT");
        User updatedUser = userRepository.save(user);

        auditLogController.logAction("ADMIN_DEMOTED_FROM_ADMIN", 0L,
                "Admin demoted user from admin: " + user.getUsername() + " (ID: " + userId + ")");

        return updatedUser;
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Game createGame(Game game) {
        if (gameExistsByName(game.getName())) {
            auditLogController.logAction("ADMIN_GAME_CREATION_FAILED", 0L,
                    "Admin game creation failed: Game name already exists - " + game.getName());
            return null;
        }

        Game createdGame = gameRepository.save(game);

        auditLogController.logAction("ADMIN_CREATED_GAME", 0L,
                "Admin created game: " + game.getName() + " (ID: " + createdGame.getId() + ")");

        return createdGame;
    }

    public boolean deleteGame(long gameId) {
        Optional<Game> gameOptional = gameRepository.findById(gameId);

        if (gameOptional.isEmpty()) {
            auditLogController.logAction("ADMIN_GAME_DELETION_FAILED", 0L,
                    "Admin game deletion failed: Game not found - ID: " + gameId);
            return false;
        }

        Game game = gameOptional.get();
        gameRepository.delete(game);

        auditLogController.logAction("ADMIN_DELETED_GAME", 0L,
                "Admin deleted game: " + game.getName() + " (ID: " + gameId + ")");

        return true;
    }

    public boolean userExistsByUsername(String username) {
        return userRepository.findAll().stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }

    public boolean userExistsByEmail(String email) {
        return userRepository.findAll().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }

    public boolean gameExistsByName(String name) {
        return gameRepository.findAll().stream()
                .anyMatch(game -> game.getName().equals(name));
    }
}
