package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.*;
import hu.benkototh.cardgame.backend.rest.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserRestService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IFriendRequestRepository friendRequestRepository;

    @Autowired
    private IFriendshipRepository friendshipRepository;

    @Autowired
    private IMessageRepository messageRepository;

    @Autowired
    private IClubMemberRepository clubMemberRepository;

    @Autowired
    private IClubMessageRepository clubMessageRepository;

    @Autowired
    private IClubInviteRepository clubInviteRepository;

    @Autowired
    private IUserHistoryRepository userHistoryRepository;

    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private ClubInviteRestService clubInviteRestService;

    private static final int MAX_LOGIN_ATTEMPTS = 5;

    @GetMapping("all")
    public ResponseEntity<List<User>> all() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/get")
    public ResponseEntity<User> getUser(@RequestParam long userId) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(user.get());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        Optional<User> foundUser = Optional.ofNullable(findByUsername(user.getUsername()));

        if (foundUser.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }

        User existingUser = foundUser.get();

        if (existingUser.isLocked()) {
            return ResponseEntity.status(401).body("Account is locked. Please contact support.");
        }

        if (passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            existingUser.setFailedLoginAttempts(0);
            userRepository.save(existingUser);
            return ResponseEntity.ok(existingUser);
        } else {
            existingUser.setFailedLoginAttempts(existingUser.getFailedLoginAttempts() + 1);

            if (existingUser.getFailedLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
                existingUser.setLocked(true);
            }

            userRepository.save(existingUser);
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> create(@RequestBody User user) {
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
            user.setRole("ROLE_USER");
            user.setLocked(false);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);

            response.put("message", "User created.");
            return ResponseEntity.ok(response);
        }
    }

    @PutMapping("/update/username")
    public ResponseEntity<Map<String, String>> updateUsername(@RequestParam long userId, @RequestParam String newUsername) {
        Optional<User> userOptional = userRepository.findById(userId);
        Map<String, String> response = new HashMap<>();

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (userExistsByUsername(newUsername)) {
                response.put("message", "Username already in use.");
                return ResponseEntity.status(400).body(response);
            } else {
                String oldUsername = user.getUsername();
                user.setUsername(newUsername);
                userRepository.save(user);

                saveUserHistory(user, oldUsername, null, "self");

                response.put("message", "Username updated.");
                return ResponseEntity.ok(response);
            }
        } else {
            response.put("message", "User not found.");
            return ResponseEntity.status(404).body(response);
        }
    }

    @PutMapping("/update/email")
    public ResponseEntity<Map<String, String>> updateEmail(@RequestParam long userId, @RequestParam String newEmail) {
        Optional<User> userOptional = userRepository.findById(userId);
        Map<String, String> response = new HashMap<>();

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (userExistsByEmail(newEmail)) {
                response.put("message", "Email already in use.");
                return ResponseEntity.status(400).body(response);
            } else {
                String oldEmail = user.getEmail();
                user.setEmail(newEmail);
                userRepository.save(user);

                saveUserHistory(user, null, oldEmail, "self");

                response.put("message", "Email updated.");
                return ResponseEntity.ok(response);
            }
        } else {
            response.put("message", "User not found.");
            return ResponseEntity.status(404).body(response);
        }
    }

    @PutMapping("/update/password")
    public ResponseEntity<Map<String, String>> updatePassword(@RequestParam long userId, @RequestParam String currentPassword, @RequestParam String newPassword) {
        Optional<User> userOptional = userRepository.findById(userId);
        Map<String, String> response = new HashMap<>();

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                response.put("message", "New password cannot be the same as the old one.");
                return ResponseEntity.status(400).body(response);
            } else if (passwordEncoder.matches(currentPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                response.put("message", "Password updated.");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Incorrect password.");
                return ResponseEntity.status(400).body(response);
            }
        } else {
            response.put("message", "User not found.");
            return ResponseEntity.status(404).body(response);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteUser(@RequestParam long userId, @RequestParam String password) {
        Optional<User> userOptional = userRepository.findById(userId);
        Map<String, String> response = new HashMap<>();

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(password, user.getPassword()) || password.equals(user.getPassword())) {
                friendRequestRepository.deleteAll(getFriendRequests(user));
                friendshipRepository.deleteAll(getFriendships(user));
                messageRepository.deleteAll(getMessages(user));
                clubMemberRepository.delete(getClubMember(user));
                clubMessageRepository.deleteAll(getClubMessages(user));
                clubInviteRepository.deleteAll(getInvites(user));
                userRepository.delete(user);
                response.put("message", "User deleted.");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Incorrect password.");
                return ResponseEntity.status(400).body(response);
            }
        } else {
            response.put("message", "User not found.");
            return ResponseEntity.status(404).body(response);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<UserHistory>> getUserHistory(@RequestParam long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        User user = userOptional.get();
        List<UserHistory> history = userHistoryRepository.findAll().stream()
                .filter(h -> h.getUser().getId() == user.getId())
                .toList();

        return ResponseEntity.ok(history);
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

    private void saveUserHistory(User user, String previousUsername, String previousEmail, String changedBy) {
        UserHistory history = new UserHistory();
        history.setUser(user);
        history.setPreviousUsername(previousUsername);
        history.setPreviousEmail(previousEmail);
        history.setChangedAt(new Date());
        history.setChangedBy(changedBy);
        userHistoryRepository.save(history);
    }

    private List<FriendRequest> getFriendRequests(User user) {
        return friendRequestRepository.findAll().stream()
                .filter(friendRequest ->
                        friendRequest.getReceiver().getId() == user.getId() ||
                                friendRequest.getSender().getId() == user.getId()
                )
                .toList();
    }

    private List<Friendship> getFriendships(User user) {
        return friendshipRepository.findAll().stream()
                .filter(friendship ->
                        friendship.getUser1().getId() == user.getId() ||
                                friendship.getUser2().getId() == user.getId()
                )
                .toList();
    }

    private List<Message> getMessages(User user) {
        return messageRepository.findAll().stream()
                .filter(message ->
                        message.getSender().getId() == user.getId() ||
                                message.getReceiver().getId() == user.getId()
                )
                .toList();
    }

    private List<ClubMessage> getClubMessages(User user) {
        return clubMessageRepository.findAll().stream()
                .filter(clubMessage -> clubMessage.getSender().getId() == user.getId())
                .toList();
    }

    private ClubMember getClubMember(User user) {
        ClubMember member = clubMemberRepository.findAll().stream()
                .filter(clubMember -> clubMember.getUser().getId() == user.getId())
                .findFirst()
                .orElse(null);

        if (member != null) {
            if (member.getRole().equals("admin")){
                ClubRestService clubRestService = new ClubRestService();
                clubRestService.deleteClub(member.getClub().getId());
            }
            return member;
        } else {
            return new ClubMember();
        }
    }

    private List<ClubInvite> getInvites(User user) {
        return clubInviteRepository.findAll().stream()
                .filter(clubInvite -> clubInvite.getUser().getId() == user.getId())
                .toList();
    }
}