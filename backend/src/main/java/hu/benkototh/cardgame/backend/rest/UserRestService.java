package hu.benkototh.cardgame.backend.rest;

import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
        if (userRepository.existsById(user.getUsername())) {
            response.put("status", "error");
            response.put("message", "User already exists.");
        } else {
            userRepository.save(user);
            response.put("status", "ok");
            response.put("message", "User created successfully.");
        }
        return response;
    }

    private boolean authenticateUser(User user) {
        return userRepository.findById(user.getUsername())
                .map(userAuth -> userAuth.getPassword().equals(user.getPassword()))
                .orElse(false);
    }
}
