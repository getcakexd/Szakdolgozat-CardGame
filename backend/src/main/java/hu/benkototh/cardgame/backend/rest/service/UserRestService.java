package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.*;
import hu.benkototh.cardgame.backend.rest.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<User> all() {
        return userRepository.findAll();
    }

    @GetMapping("/get")
    public User getUser(@RequestParam long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        Optional<User> foundUser = Optional.ofNullable(findByUsername(user.getUsername()));

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
    public Map<String, String> updateUsername(@RequestParam long userId, @RequestParam String newUsername) {
        Map<String, String> response = new HashMap<>();
        Optional<User> userOptional = userRepository.findById(userId);

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
    public Map<String, String> updateEmail(@RequestParam long userId, @RequestParam String newEmail) {
        Map<String, String> response = new HashMap<>();
        Optional<User> userOptional = userRepository.findById(userId);

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
    public Map<String, String> updatePassword(@RequestParam long userId, @RequestParam String currentPassword, @RequestParam String newPassword) {
        Map<String, String> response = new HashMap<>();
        Optional<User> userOptional = userRepository.findById(userId);

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
    public Map<String, String> deleteUser(@RequestParam long userId, @RequestParam String password) {
        Map<String, String> response = new HashMap<>();
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
