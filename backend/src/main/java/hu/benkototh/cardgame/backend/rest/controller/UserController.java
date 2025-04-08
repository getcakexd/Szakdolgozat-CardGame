package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.*;
import hu.benkototh.cardgame.backend.rest.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class UserController {

    @Autowired
    public IUserRepository userRepository;

    @Autowired
    private IUserHistoryRepository userHistoryRepository;

    @Lazy
    @Autowired
    private FriendRequestController friendRequestController;
    
    @Lazy
    @Autowired
    private FriendshipController friendshipController;
    
    @Lazy
    @Autowired
    private ChatController chatController;
    
    @Lazy
    @Autowired
    private ClubMemberController clubMemberController;
    
    @Lazy
    @Autowired
    private ClubChatController clubChatController;
    
    @Lazy
    @Autowired
    private ClubInviteController clubInviteController;
    
    @Lazy
    @Autowired
    private ClubController clubController;
    
    @Autowired
    private AuditLogController auditLogController;

    public BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final int MAX_LOGIN_ATTEMPTS = 5;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUser(long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public User login(User user) {
        Optional<User> foundUser = Optional.ofNullable(findByUsername(user.getUsername()));

        if (foundUser.isEmpty()) {
            auditLogController.logAction("LOGIN_FAILED", user.getId(),
                    "Login failed: User not found - " + user.getUsername());
            return null;
        }

        User existingUser = foundUser.get();

        if (existingUser.isLocked()) {
            auditLogController.logAction("LOGIN_FAILED", existingUser.getId(),
                    "Login failed: Account locked - " + existingUser.getUsername());
            return null;
        }

        if (passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            existingUser.setFailedLoginAttempts(0);
            auditLogController.logAction("LOGIN_SUCCESS", existingUser.getId(),
                    "User logged in successfully: " + existingUser.getUsername());
            return userRepository.save(existingUser);
        } else {
            existingUser.setFailedLoginAttempts(existingUser.getFailedLoginAttempts() + 1);

            if (existingUser.getFailedLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
                existingUser.setLocked(true);
                auditLogController.logAction("ACCOUNT_LOCKED", existingUser.getId(),
                        "Account locked due to too many failed login attempts: " + existingUser.getUsername());
            } else {
                auditLogController.logAction("LOGIN_FAILED", existingUser.getId(),
                        "Login failed: Incorrect password - " + existingUser.getUsername() + 
                        " (Attempt " + existingUser.getFailedLoginAttempts() + " of " + MAX_LOGIN_ATTEMPTS + ")");
            }

            userRepository.save(existingUser);
            return null;
        }
    }

    public User createUser(User user) {
        if (userExistsByUsername(user.getUsername()) || userExistsByEmail(user.getEmail())) {
            auditLogController.logAction("USER_CREATION_FAILED", 0L,
                    "User creation failed: Username or email already exists - " + user.getUsername());
            return null;
        }
        
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setRole("ROLE_USER");
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        User savedUser = userRepository.save(user);
        
        auditLogController.logAction("USER_CREATED", savedUser.getId(),
                "New user created: " + savedUser.getUsername());
        
        return savedUser;
    }

    public User updateUsername(long userId, String newUsername) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty() || userExistsByUsername(newUsername)) {
            auditLogController.logAction("USERNAME_UPDATE_FAILED", userId,
                    "Username update failed: User not found or username already exists - " + newUsername);
            return null;
        }
        
        User user = userOptional.get();
        String oldUsername = user.getUsername();
        user.setUsername(newUsername);
        User updatedUser = userRepository.save(user);
        
        saveUserHistory(user, oldUsername, null, "self");
        
        auditLogController.logAction("USERNAME_UPDATED", userId,
                "Username updated from " + oldUsername + " to " + newUsername);
        
        return updatedUser;
    }

    public User updateEmail(long userId, String newEmail) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty() || userExistsByEmail(newEmail)) {
            auditLogController.logAction("EMAIL_UPDATE_FAILED", userId,
                    "Email update failed: User not found or email already exists - " + newEmail);
            return null;
        }
        
        User user = userOptional.get();
        String oldEmail = user.getEmail();
        user.setEmail(newEmail);
        User updatedUser = userRepository.save(user);
        
        saveUserHistory(user, null, oldEmail, "self");
        
        auditLogController.logAction("EMAIL_UPDATED", userId,
                "Email updated from " + oldEmail + " to " + newEmail);
        
        return updatedUser;
    }

    public User updatePassword(long userId, String currentPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            auditLogController.logAction("PASSWORD_UPDATE_FAILED", userId,
                    "Password update failed: User not found");
            return null;
        }
        
        User user = userOptional.get();
        
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            auditLogController.logAction("PASSWORD_UPDATE_FAILED", userId,
                    "Password update failed: New password same as current");
            return null;
        }
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            auditLogController.logAction("PASSWORD_UPDATE_FAILED", userId,
                    "Password update failed: Current password incorrect");
            return null;
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        User updatedUser = userRepository.save(user);
        
        auditLogController.logAction("PASSWORD_UPDATED", userId,
                "Password updated successfully");
        
        return updatedUser;
    }

    public boolean deleteUser(long userId, String password) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            auditLogController.logAction("USER_DELETION_FAILED", userId,
                    "User deletion failed: User not found");
            return false;
        }
        
        User user = userOptional.get();
        
        if (!passwordEncoder.matches(password, user.getPassword()) && !password.equals(user.getPassword())) {
            auditLogController.logAction("USER_DELETION_FAILED", userId,
                    "User deletion failed: Password incorrect");
            return false;
        }

        friendRequestController.deleteFriendRequestsByUser(user);
        friendshipController.deleteFriendshipsByUser(user);
        chatController.deleteMessagesByUser(user);

        ClubMember clubMember = clubMemberController.getClubMemberByUser(user);
        if (clubMember != null) {
            if (clubMember.getRole().equals("admin")) {
                clubController.deleteClub(clubMember.getClub().getId());
            }
            clubMemberController.deleteClubMember(clubMember);
        }
        
        clubChatController.deleteMessagesByUser(user);
        clubInviteController.deleteInvitesByUser(user);

        userRepository.delete(user);
        
        auditLogController.logAction("USER_DELETED", user.getId(),
                "User deleted: " + user.getUsername() + " (ID: " + userId + ")");
        
        return true;
    }

    public List<UserHistory> getUserHistory(long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();
                
        return userHistoryRepository.findAll().stream()
                .filter(h -> h.getUser().getId() == user.getId())
                .toList();
    }

    public boolean authenticateUser(User user) {
        boolean result = userRepository.findById(user.getId())
                .map(userAuth -> passwordEncoder.matches(user.getPassword(), userAuth.getPassword()))
                .orElse(false);
                
        if (result) {
            auditLogController.logAction("USER_AUTHENTICATED", user.getId(),
                    "User authenticated successfully");
        } else {
            auditLogController.logAction("USER_AUTHENTICATION_FAILED", user.getId(),
                    "User authentication failed");
        }
        
        return result;
    }

    public User findByUsername(String username) {
        return userRepository.findAll().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public boolean userExistsByUsername(String username) {
        List<User> users = userRepository.findAll();

        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return true;
            }
        }

        return false;
    }

    public boolean userExistsByEmail(String email) {
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
        
        auditLogController.logAction("USER_HISTORY_RECORDED", user.getId(),
                "User history recorded: " + (previousUsername != null ? "Username changed" : "Email changed"));
    }
}
