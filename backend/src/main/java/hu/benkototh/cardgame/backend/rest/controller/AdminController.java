package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.Game;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IGameRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
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
    private UserController userController;

    @Autowired
    private IGameRepository gameRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user) {
        if (userExistsByUsername(user.getUsername()) || userExistsByEmail(user.getEmail())) {
            return null;
        }
        
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        return userRepository.save(user);
    }

    public boolean deleteUser(long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isEmpty()) {
            return false;
        }
        
        userController.deleteUser(userOptional.get().getId(), userOptional.get().getPassword());
        return true;
    }

    public User promoteToAgent(long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();

        if (!user.getRole().equals("ROLE_USER")) {
            return null;
        }

        user.setRole("ROLE_AGENT");
        return userRepository.save(user);
    }

    public User demoteFromAgent(long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();

        if (!user.getRole().equals("ROLE_AGENT")) {
            return null;
        }

        user.setRole("ROLE_USER");
        return userRepository.save(user);
    }

    public User promoteToAdmin(long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();

        if (user.getRole().equals("ROLE_ADMIN") || user.getRole().equals("ROLE_ROOT")) {
            return null;
        }

        user.setRole("ROLE_ADMIN");
        return userRepository.save(user);
    }

    public User demoteFromAdmin(long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();

        if (!user.getRole().equals("ROLE_ADMIN")) {
            return null;
        }

        user.setRole("ROLE_AGENT");
        return userRepository.save(user);
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Game createGame(Game game) {
        if (gameExistsByName(game.getName())) {
            return null;
        }
        
        return gameRepository.save(game);
    }

    public boolean deleteGame(long gameId) {
        Optional<Game> gameOptional = gameRepository.findById(gameId);
        
        if (gameOptional.isEmpty()) {
            return false;
        }
        
        gameRepository.delete(gameOptional.get());
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
