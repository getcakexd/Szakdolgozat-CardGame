package hu.benkototh.cardgame.backend.rest;

import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import hu.benkototh.cardgame.backend.rest.Data.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserRestService {

    @Autowired
    private IUserRepository userRepository;

    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @GetMapping("all")
    public List<User> all() {
        return userRepository.findAll();
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        Optional<User> foundUser = userRepository.findById(findByUsername(user.getUsername()).getId());

        if (foundUser.isPresent() && passwordEncoder.matches(user.getPassword(), foundUser.get().getPassword())) {
            User loggedInUser = foundUser.get();
            response.put("status", "ok");
            response.put("userId", String.valueOf(loggedInUser.getId()));
            response.put("email", loggedInUser.getEmail());
        } else {
            response.put("status", "error");
            response.put("message", "Invalid username or password");
        }
        return response;
    }


    @PostMapping("/create")
    public Map<String, String> create(@RequestBody User user) {
        Map<String, String> response = new HashMap<>();

        if (userExistsByUsername(user.getUsername())) {
            response.put("status", "error");
            response.put("message", "User already in use.");
        } else if (userExistsByEmail(user.getUsername())) {
            response.put("status", "error");
            response.put("message", "Email already in use.");
        } else {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            user.setRole("ROLE_USER");
            userRepository.save(user);
            response.put("status", "ok");
            response.put("message", "User created successfully.");
        }
        return response;
    }

    @PutMapping("/update/username")
    public Map<String, String> updateUsername(@RequestParam String userId, @RequestParam String newUsername) {
        Map<String, String> response = new HashMap<>();
        Long userIdLong = Long.parseLong(userId);
        Optional<User> userOptional = userRepository.findById(userIdLong);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (userExistsByUsername(newUsername)) {
                response.put("status", "error");
                response.put("message", "Username already in use.");
            } else {
                user.setUsername(newUsername);
                userRepository.save(user);
                response.put("status", "ok");
                response.put("message", "Username updated.");
            }
        } else {
            response.put("status", "error");
            response.put("message", "User not found.");
        }
        return response;
    }

    @PutMapping("/update/email")
    public Map<String, String> updateEmail(@RequestParam String userId, @RequestParam String newEmail) {
        Map<String, String> response = new HashMap<>();
        Long userIdLong = Long.parseLong(userId);
        Optional<User> userOptional = userRepository.findById(userIdLong);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (userExistsByEmail(newEmail)) {
                response.put("status", "error");
                response.put("message", "Email already in use.");
            } else {
                user.setEmail(newEmail);
                userRepository.save(user);
                response.put("status", "ok");
                response.put("message", "Email updated.");
            }
        } else {
            response.put("status", "error");
            response.put("message", "User not found.");
        }
        return response;
    }

    @PutMapping("/update/password")
    public Map<String, String> updatePassword(@RequestParam String userId, @RequestParam String currentPassword, @RequestParam String newPassword) {
        Map<String, String> response = new HashMap<>();
        Long userIdLong = Long.parseLong(userId);
        Optional<User> userOptional = userRepository.findById(userIdLong);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                response.put("status", "error");
                response.put("message", "New password must be different from the current one.");
            } else if (passwordEncoder.matches(currentPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                response.put("status", "ok");
                response.put("message", "Password updated.");
            } else {
                response.put("status", "error");
                response.put("message", "Current password is incorrect.");
            }
        } else {
            response.put("status", "error");
            response.put("message", "User not found.");
        }
        return response;
    }

    @DeleteMapping("/delete")
    public Map<String, String> deleteUser(@RequestParam String userId, @RequestParam String password) {
        Map<String, String> response = new HashMap<>();
        Long userIdLong = Long.parseLong(userId);
        Optional<User> userOptional = userRepository.findById(userIdLong);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                userRepository.delete(user);
                response.put("status", "ok");
                response.put("message", "Account deleted.");
            } else {
                response.put("status", "error");
                response.put("message", "Incorrect password.");
            }
        } else {
            response.put("status", "error");
            response.put("message", "User not found.");
        }
        return response;
    }

    private boolean authenticateUser(User user) {
        return userRepository.findById(user.getId())
                .map(userAuth -> passwordEncoder.matches(user.getPassword(), userAuth.getPassword()))
                .orElse(false);
    }

    private User findByUsername(String username) {
        return userRepository.findAll().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    private boolean userExistsByUsername(String username) {
        List<User> users = userRepository.findAll();

        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return true;
            }
        }

        return false;
    }

    private boolean userExistsByEmail(String email) {
        List<User> users = userRepository.findAll();

        for (User u : users) {
            if (u.getEmail().equals(email)) {
                return true;
            }
        }

        return false;
    }
}
