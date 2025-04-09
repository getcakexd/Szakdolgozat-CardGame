package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.*;
import hu.benkototh.cardgame.backend.rest.repository.*;
import hu.benkototh.cardgame.backend.rest.util.GoogleTokenVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Autowired
    private GoogleTokenVerifier googleTokenVerifier;

    public BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final String GOOGLE_AUTH_PREFIX = "google_auth_";
    private static final String GOOGLE_AUTH_SUFFIX = "54b96774-c25f-460f-9c01-aaf10a394dd9";

    public List<User> getAllUsers() {
        auditLogController.logAction("ALL_USERS_VIEWED", 0L, "All users viewed");
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

    public User loginWithGoogle(GoogleAuthRequest googleAuthRequest) {
        try {
            var payload = googleTokenVerifier.verify(googleAuthRequest.getToken());

            if (payload == null) {
                auditLogController.logAction("GOOGLE_LOGIN_FAILED", 0L,
                        "Google login failed: Invalid token");
                return null;
            }

            String email = payload.get("email").toString();

            synchronized (this) {
                User existingUser = findByEmail(email);

                if (existingUser != null) {
                    auditLogController.logAction("GOOGLE_LOGIN_SUCCESS", existingUser.getId(),
                            "User logged in with Google: " + existingUser.getUsername());
                    return existingUser;
                } else {
                    User newUser = new User();
                    newUser.setEmail(email);

                    String name = googleAuthRequest.getName();
                    String username = generateUsername(name != null ? name : email);
                    newUser.setUsername(username);

                    String randomPassword = GOOGLE_AUTH_PREFIX + GOOGLE_AUTH_SUFFIX;
                    newUser.setPassword(passwordEncoder.encode(randomPassword));

                    newUser.setRole("ROLE_USER");
                    newUser.setLocked(false);
                    newUser.setFailedLoginAttempts(0);

                    User savedUser = userRepository.save(newUser);

                    auditLogController.logAction("USER_CREATED_VIA_GOOGLE", savedUser.getId(),
                            "New user created via Google: " + savedUser.getUsername());

                    return savedUser;
                }
            }
        } catch (Exception e) {
            auditLogController.logAction("GOOGLE_LOGIN_FAILED", 0L,
                    "Google login failed: " + e.getMessage());
            return null;
        }
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

    public boolean hasGoogleAuthPassword(long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();

        String testPassword = GOOGLE_AUTH_PREFIX + GOOGLE_AUTH_SUFFIX;

        return passwordEncoder.matches(testPassword, user.getPassword());
    }

    public User setPassword(long userId, String newPassword) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            auditLogController.logAction("PASSWORD_SET_FAILED", userId,
                    "Password set failed: User not found");
            return null;
        }

        User user = userOptional.get();

        user.setPassword(passwordEncoder.encode(newPassword));
        User updatedUser = userRepository.save(user);

        auditLogController.logAction("PASSWORD_SET", userId,
                "Password set successfully for Google auth user");

        return updatedUser;
    }

    public List<UserHistory> getUserHistory(long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();
        
        auditLogController.logAction("USER_HISTORY_VIEWED", userId,
                "User history viewed");
                
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


    public User findByEmail(String email) {
        return userRepository.findAll().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    private String generateUsername(String base) {
        String cleanBase = base.replaceAll("[^a-zA-Z0-9]", "");

        if (!userExistsByUsername(cleanBase)) {
            return cleanBase;
        }

        for (int i = 1; i <= 100; i++) {
            String username = cleanBase + i;
            if (!userExistsByUsername(username)) {
                return username;
            }
        }

        return "user_" + UUID.randomUUID().toString().substring(0, 8);
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
