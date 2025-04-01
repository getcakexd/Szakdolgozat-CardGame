package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.*;
import hu.benkototh.cardgame.backend.rest.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

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

    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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

        if (foundUser.isPresent() && passwordEncoder.matches(user.getPassword(), foundUser.get().getPassword())) {
            return ResponseEntity.ok(foundUser.get());
        } else {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }


    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestBody User user) {

        if (userExistsByUsername(user.getUsername())) {
            return ResponseEntity.status(400).body("Username already in use.");
        } else if (userExistsByEmail(user.getUsername())) {
            return ResponseEntity.status(400).body("Email already in use.");
        } else {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            user.setRole("ROLE_USER");
            userRepository.save(user);
            return ResponseEntity.ok("User created.");

        }
    }

    @PutMapping("/update/username")
    public ResponseEntity<String> updateUsername(@RequestParam long userId, @RequestParam String newUsername) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (userExistsByUsername(newUsername)) {
                return ResponseEntity.status(400).body("Username already in use.");
            } else {
                user.setUsername(newUsername);
                userRepository.save(user);
                return ResponseEntity.ok("Username updated.");
            }
        } else {
            return ResponseEntity.status(404).body("User not found.");
        }
    }

    @PutMapping("/update/email")
    public ResponseEntity<String> updateEmail(@RequestParam long userId, @RequestParam String newEmail) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (userExistsByEmail(newEmail)) {
                return ResponseEntity.status(400).body("Email already in use.");
            } else {
                user.setEmail(newEmail);
                userRepository.save(user);
                return ResponseEntity.ok("Email updated.");
            }
        } else {
            return ResponseEntity.status(404).body("User not found.");
        }
    }

    @PutMapping("/update/password")
    public ResponseEntity<String> updatePassword(@RequestParam long userId, @RequestParam String currentPassword, @RequestParam String newPassword) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                return ResponseEntity.status(400).body("New password cannot be the same as the old one.");
            } else if (passwordEncoder.matches(currentPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return ResponseEntity.ok("Password updated.");
            } else {
                return ResponseEntity.status(400).body("Incorrect password.");
            }
        } else {
            return ResponseEntity.status(404).body("User not found.");
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(@RequestParam long userId, @RequestParam String password) {
        Optional<User> userOptional = userRepository.findById(userId);



        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                friendRequestRepository.deleteAll(getFriendRequests(user));
                friendshipRepository.deleteAll(getFriendships(user));
                messageRepository.deleteAll(getMessages(user));
                clubMemberRepository.delete(getClubMember(user));
                clubMessageRepository.deleteAll(getClubMessages(user));
                userRepository.delete(user);
                return ResponseEntity.ok("User deleted.");
            } else {
                return ResponseEntity.status(400).body("Incorrect password.");
            }
        } else {
            return ResponseEntity.status(404).body("User not found.");
        }
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
}
