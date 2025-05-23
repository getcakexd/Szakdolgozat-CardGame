package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.game.controller.StatsController;
import hu.benkototh.cardgame.backend.game.model.GameStatistics;
import hu.benkototh.cardgame.backend.game.model.UserGameStats;
import hu.benkototh.cardgame.backend.game.model.UserStats;
import hu.benkototh.cardgame.backend.rest.model.*;
import hu.benkototh.cardgame.backend.rest.repository.*;
import hu.benkototh.cardgame.backend.rest.util.GoogleTokenVerifier;
import hu.benkototh.cardgame.backend.rest.service.SimpleEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class UserController {

    @Autowired
    public IUserRepository userRepository;

    @Autowired
    private IUserHistoryRepository userHistoryRepository;

    @Autowired
    private AuditLogController auditLogController;

    @Autowired
    private GoogleTokenVerifier googleTokenVerifier;

    @Autowired
    private SimpleEmailService emailService;

    @Autowired
    private StatsController statsController;

    @Autowired
    private FriendshipController friendshipController;

    @Autowired
    private ChatController chatController;

    @Autowired
    private ClubMemberController clubMemberController;

    public BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final int MAX_LOGIN_ATTEMPTS = 5;

    @Value("${google.oauth.password.prefix}")
    private String GOOGLE_AUTH_PREFIX;

    @Value("${google.oauth.password.suffix}")
    private String GOOGLE_AUTH_SUFFIX;

    @Value("${app.verification.token-expiration:24}")
    private int verificationTokenExpirationHours;
    @Autowired
    private LobbyController lobbyController;
    @Autowired
    private TicketController ticketController;
    @Autowired
    private ClubChatController clubChatController;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUser(long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public User login(User user) {
        Optional<User> foundUser = Optional.ofNullable(findByEmail(user.getEmail()));

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

        if (!existingUser.isVerified()) {
            auditLogController.logAction("LOGIN_FAILED", existingUser.getId(),
                    "Login failed: Email not verified - " + existingUser.getUsername());
            return null;
        }

        if (passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            existingUser.setFailedLoginAttempts(0);
            auditLogController.logAction("LOGIN_SUCCESS", existingUser.getId(),
                    "User logged in successfully: " + existingUser.getUsername());
            return existingUser;
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
        setRoleForUSer(user);
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        user.setVerified(false);

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, verificationTokenExpirationHours);
        user.setVerificationTokenExpiry(calendar.getTime());

        User savedUser = userRepository.save(user);

        try {
            emailService.sendVerificationEmail(user.getEmail(), token, savedUser.getId());
            auditLogController.logAction("VERIFICATION_EMAIL_SENT", savedUser.getId(),
                    "Verification email sent to: " + savedUser.getEmail());
        } catch (Exception e) {
            auditLogController.logAction("VERIFICATION_EMAIL_FAILED", savedUser.getId(),
                    "Failed to send verification email: " + e.getMessage());
        }

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

                    setRoleForUSer(newUser);
                    newUser.setLocked(false);
                    newUser.setFailedLoginAttempts(0);
                    newUser.setVerified(true);

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

    public Map<String, Object> getUserProfileData(long userId) {
        Map<String, Object> profileData = new HashMap<>();

        User user = getUser(userId);
        if (user == null) {
            return null;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("role", user.getRole());
        userData.put("verified", user.isVerified());
        profileData.put("user", userData);

        UserStats userStats = statsController.getUserStats(userId);
        if (userStats != null) {
            Map<String, Object> sanitizedStats = new HashMap<>();
            sanitizedStats.put("gamesPlayed", userStats.getGamesPlayed());
            sanitizedStats.put("gamesWon", userStats.getGamesWon());
            sanitizedStats.put("gamesLost", userStats.getGamesLost());
            sanitizedStats.put("gamesAbandoned", userStats.getGamesAbandoned());
            sanitizedStats.put("totalPoints", userStats.getTotalPoints());
            sanitizedStats.put("currentWinStreak", userStats.getCurrentWinStreak());
            sanitizedStats.put("biggestWinStreak", userStats.getBiggestWinStreak());
            sanitizedStats.put("totalFatsCollected", userStats.getTotalFatsCollected());
            profileData.put("stats", sanitizedStats);
        }

        List<UserGameStats> gameStats = statsController.getUserGameStats(userId);
        if (gameStats != null && !gameStats.isEmpty()) {
            List<Map<String, Object>> sanitizedGameStats = gameStats.stream()
                    .map(stat -> {
                        Map<String, Object> gameStatData = new HashMap<>();
                        gameStatData.put("gamesPlayed", stat.getGamesPlayed());
                        gameStatData.put("gamesWon", stat.getGamesWon());
                        gameStatData.put("gamesLost", stat.getGamesLost());
                        gameStatData.put("gamesAbandoned", stat.getGamesAbandoned());
                        gameStatData.put("totalPoints", stat.getTotalPoints());
                        gameStatData.put("highestScore", stat.getHighestScore());
                        gameStatData.put("totalFatsCollected", stat.getTotalFatsCollected());
                        gameStatData.put("highestFatsInGame", stat.getHighestFatsInGame());
                        gameStatData.put("currentWinStreak", stat.getCurrentWinStreak());
                        gameStatData.put("biggestWinStreak", stat.getBiggestWinStreak());
                        gameStatData.put("lastPlayed", stat.getLastPlayed());

                        if (stat.getGameDefinition() != null) {
                            gameStatData.put("gameId", stat.getGameDefinition().getId());
                            gameStatData.put("gameName", stat.getGameDefinition().getName());
                        }

                        return gameStatData;
                    })
                    .collect(Collectors.toList());
            profileData.put("gameStats", sanitizedGameStats);
        }

        List<GameStatistics> recentGames = statsController.getRecentGames(userId, 10);
        if (recentGames != null && !recentGames.isEmpty()) {
            List<Map<String, Object>> sanitizedRecentGames = recentGames.stream()
                    .map(game -> {
                        Map<String, Object> gameData = new HashMap<>();
                        gameData.put("gameId", game.getGameId());
                        gameData.put("gameDefinitionId", game.getGameDefinitionId());
                        gameData.put("gameType", game.getGameType());
                        gameData.put("score", game.getScore());
                        gameData.put("won", game.isWon());
                        gameData.put("fatCardsCollected", game.getFatCardsCollected());
                        gameData.put("tricksTaken", game.getTricksTaken());
                        gameData.put("playedAt", game.getPlayedAt());
                        return gameData;
                    })
                    .collect(Collectors.toList());
            profileData.put("recentGames", sanitizedRecentGames);
        }

        List<User> friends = friendshipController.getFriends(userId);
        if (friends != null && !friends.isEmpty()) {
            List<Map<String, Object>> sanitizedFriends = friends.stream()
                    .map(friend -> {
                        Map<String, Object> friendData = new HashMap<>();
                        friendData.put("id", friend.getId());
                        friendData.put("username", friend.getUsername());
                        return friendData;
                    })
                    .collect(Collectors.toList());
            profileData.put("friends", sanitizedFriends);
        }

        List<Message> messages = chatController.getSentMessagesByUser(userId);
        if (messages != null && !messages.isEmpty()) {
            List<Map<String, Object>> sanitizedMessages = messages.stream()
                    .map(message -> {
                        Map<String, Object> messageData = new HashMap<>();
                        messageData.put("id", message.getId());
                        messageData.put("content", message.getContent());
                        messageData.put("timestamp", message.getTimestamp());
                        messageData.put("status", message.getStatus());

                        if (message.getReceiver() != null) {
                            messageData.put("receiverUsername", message.getReceiver().getUsername());
                            messageData.put("receiverId", message.getReceiver().getId());
                        }

                        return messageData;
                    })
                    .collect(Collectors.toList());
            profileData.put("messages", sanitizedMessages);
        }

        List<Club> clubs = clubMemberController.getClubsByUser(user);
        if (clubs != null && !clubs.isEmpty()) {
            List<Map<String, Object>> clubData = clubs.stream()
                    .map(club -> {
                        Map<String, Object> clubInfo = new HashMap<>();
                        clubInfo.put("id", club.getId());
                        clubInfo.put("name", club.getName());
                        clubInfo.put("role", clubMemberController.getClubMemberRole(club.getId(), userId));
                        return clubInfo;
                    })
                    .collect(Collectors.toList());
            profileData.put("clubs", clubData);
        }

        List<ClubMessage> clubMessages = clubChatController.getMessagesByUser(userId);
        if (clubMessages != null && !clubMessages.isEmpty()) {
            List<Map<String, Object>> sanitizedClubMessages = clubMessages.stream()
                    .map(message -> {
                        Map<String, Object> messageData = new HashMap<>();
                        messageData.put("id", message.getId());
                        messageData.put("content", message.getContent());
                        messageData.put("timestamp", message.getTimestamp());
                        messageData.put("status", message.getStatus());

                        if (message.getClub() != null) {
                            messageData.put("clubName", message.getClub().getName());
                            messageData.put("clubId", message.getClub().getId());
                        }

                        return messageData;
                    })
                    .collect(Collectors.toList());
            profileData.put("clubMessages", sanitizedClubMessages);
        }

        List<Ticket> tickets = ticketController.getUserTickets(userId);
        if (tickets != null && !tickets.isEmpty()) {
            List<Map<String, Object>> sanitizedTickets = tickets.stream()
                    .map(ticket -> {
                        Map<String, Object> ticketData = new HashMap<>();
                        ticketData.put("id", ticket.getId());
                        ticketData.put("reference", ticket.getReference());
                        ticketData.put("subject", ticket.getSubject());
                        ticketData.put("category", ticket.getCategory());
                        ticketData.put("status", ticket.getStatus());
                        ticketData.put("createdAt", ticket.getCreatedAt());
                        ticketData.put("updatedAt", ticket.getUpdatedAt());

                        if (ticket.getAssignedTo() != null) {
                            ticketData.put("assignedToUsername", ticket.getAssignedTo().getUsername());
                        }

                        return ticketData;
                    })
                    .collect(Collectors.toList());
            profileData.put("tickets", sanitizedTickets);
        }

        List<TicketMessage> ticketMessages = ticketController.getMessagesByUser(userId);
        if (ticketMessages != null && !ticketMessages.isEmpty()) {
            List<Map<String, Object>> sanitizedTicketMessages = ticketMessages.stream()
                    .map(message -> {
                        Map<String, Object> messageData = new HashMap<>();
                        messageData.put("id", message.getId());
                        messageData.put("message", message.getMessage());
                        messageData.put("senderName", message.getSenderName());
                        messageData.put("senderType", message.getSenderType());
                        messageData.put("createdAt", message.getCreatedAt());
                        messageData.put("fromAgent", message.isFromAgent());

                        if (message.getTicket() != null) {
                            messageData.put("ticketReference", message.getTicket().getReference());
                            messageData.put("ticketId", message.getTicket().getId());
                        }

                        return messageData;
                    })
                    .collect(Collectors.toList());
            profileData.put("ticketMessages", sanitizedTicketMessages);
        }

        auditLogController.logAction("PROFILE_DATA_ACCESSED", userId,
                "User profile data accessed for user: " + userId);

        return profileData;
    }

    public boolean verifyEmail(String token) {
        User user = findByVerificationToken(token);

        if (user == null) {
            return false;
        }

        if (user.getVerificationTokenExpiry().before(new Date())) {
            return false;
        }

        user.setVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);

        auditLogController.logAction("EMAIL_VERIFIED", user.getId(),
                "Email verified for user: " + user.getUsername());

        return true;
    }

    public boolean resendVerificationEmail(long userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();

        if (user.isVerified()) {
            return false;
        }

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, verificationTokenExpirationHours);
        user.setVerificationTokenExpiry(calendar.getTime());

        userRepository.save(user);

        try {
            emailService.sendVerificationEmail(user.getEmail(), token, user.getId());
            auditLogController.logAction("VERIFICATION_EMAIL_RESENT", user.getId(),
                    "Verification email resent to: " + user.getEmail());
            return true;
        } catch (Exception e) {
            auditLogController.logAction("VERIFICATION_EMAIL_FAILED", user.getId(),
                    "Failed to resend verification email: " + e.getMessage());
            return false;
        }
    }

    public User findByVerificationToken(String token) {
        return userRepository.findAll().stream()
                .filter(user -> token.equals(user.getVerificationToken()))
                .findFirst()
                .orElse(null);
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

        user.setVerified(false);
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, verificationTokenExpirationHours);
        user.setVerificationTokenExpiry(calendar.getTime());

        User updatedUser = userRepository.save(user);

        try {
            emailService.sendVerificationEmail(newEmail, token, user.getId());
            auditLogController.logAction("VERIFICATION_EMAIL_SENT", user.getId(),
                    "Verification email sent to new email: " + newEmail);
        } catch (Exception e) {
            auditLogController.logAction("VERIFICATION_EMAIL_FAILED", user.getId(),
                    "Failed to send verification email to new email: " + e.getMessage());
        }

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

    public void logDataAccess(long userId, String dataType, String action) {
        auditLogController.logAction("DATA_ACCESS", userId,
                "User accessed " + dataType + " data with action: " + action);
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

    private void setRoleForUSer(User user) {
        if (userRepository.findAll().isEmpty()){
            user.setRole("ROLE_ROOT");

            auditLogController.logAction("USER_ROLE_SET", user.getId(),
                    "User role set to " + "ROLE_ROOT" + ": " + user.getUsername());
        } else {
            user.setRole("ROLE_USER");

            auditLogController.logAction("USER_ROLE_SET", user.getId(),
                    "User role set to " + "ROLE_USER" + ": " + user.getUsername());
        }
    }
}
