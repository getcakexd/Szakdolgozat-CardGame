package hu.benkototh.cardgame.backend.rest;

import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import hu.benkototh.cardgame.backend.rest.Data.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Map<String, String> login(@RequestBody User user) {
        Map<String, String> response = new HashMap<>();
        if (authenticateUser(user)) {
            response.put("status", "ok");
        } else {
            response.put("status", "error");
        }
        return response;
    }

    @PostMapping("/create")
    public Map<String, String> create(@RequestBody User user) {
        Map<String, String> response = new HashMap<>();

        if (userExistsByUsername(user)) {
            response.put("status", "error");
            response.put("message", "User already in use.");
        } else if (userExistsByEmail(user)) {
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

    private boolean authenticateUser(User user) {
        return userRepository.findById(user.getId())
                .map(userAuth -> passwordEncoder.matches(user.getPassword(), userAuth.getPassword()))
                .orElse(false);
    }

    private boolean userExistsByUsername(User user) {
        List<User> users = userRepository.findAll();

        for (User u : users) {
            if (u.getUsername().equals(user.getUsername())) {
                return true;
            }
        }

        return false;
    }

    private boolean userExistsByEmail(User user) {
        List<User> users = userRepository.findAll();

        for (User u : users) {
            if (u.getEmail().equals(user.getEmail())) {
                return true;
            }
        }

        return false;
    }
}
