package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.Data.UserHistory;
import hu.benkototh.cardgame.backend.rest.repository.IUserHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.Map;

@Controller
public class AgentController {

    @Lazy
    @Autowired
    private UserController userController;

    @Autowired
    private IUserHistoryRepository userHistoryRepository;

    @Autowired
    private AuditLogController auditLogController;

    public User unlockUser(long userId) {
        User user = userController.getUser(userId);

        if (user == null) {
            auditLogController.logAction("USER_UNLOCK_FAILED", 0L,
                    "User unlock failed: User not found - ID: " + userId);
            return null;
        }

        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        User updatedUser = userController.userRepository.save(user);

        auditLogController.logAction("USER_UNLOCKED", 0L,
                "User unlocked: " + user.getUsername() + " (ID: " + userId + ")");

        return updatedUser;
    }

    public User modifyUserData(Map<String, Object> userData) {
        if (!userData.containsKey("userId")) {
            auditLogController.logAction("USER_MODIFY_FAILED", 0L,
                    "User modification failed: Missing user ID");
            return null;
        }

        long userId = Long.parseLong(userData.get("userId").toString());
        User user = userController.getUser(userId);

        if (user == null) {
            auditLogController.logAction("USER_MODIFY_FAILED", 0L,
                    "User modification failed: User not found - ID: " + userId);
            return null;
        }

        String previousUsername = null;
        String previousEmail = null;
        boolean modified = false;

        if (userData.containsKey("username") && !userData.get("username").equals(user.getUsername())) {
            String newUsername = userData.get("username").toString();

            if (userController.userExistsByUsername(newUsername)) {
                auditLogController.logAction("USER_MODIFY_FAILED", 0L,
                        "User modification failed: Username already exists - " + newUsername);
                return null;
            }

            previousUsername = user.getUsername();
            user.setUsername(newUsername);
            modified = true;

            auditLogController.logAction("USERNAME_MODIFIED_BY_AGENT", 0L,
                    "Username modified by agent for user ID " + userId + ": " + previousUsername + " -> " + newUsername);
        }

        if (userData.containsKey("email") && !userData.get("email").equals(user.getEmail())) {
            String newEmail = userData.get("email").toString();

            if (userController.userExistsByEmail(newEmail)) {
                auditLogController.logAction("USER_MODIFY_FAILED", 0L,
                        "User modification failed: Email already exists - " + newEmail);
                return null;
            }

            previousEmail = user.getEmail();
            user.setEmail(newEmail);
            modified = true;

            auditLogController.logAction("EMAIL_MODIFIED_BY_AGENT", 0L,
                    "Email modified by agent for user ID " + userId + ": " + previousEmail + " -> " + newEmail);
        }

        if (modified) {
            userController.userRepository.save(user);

            UserHistory history = new UserHistory();
            history.setUser(user);
            history.setPreviousUsername(previousUsername);
            history.setPreviousEmail(previousEmail);
            history.setChangedAt(new Date());
            history.setChangedBy("agent");
            userHistoryRepository.save(history);

            auditLogController.logAction("USER_HISTORY_RECORDED_BY_AGENT", 0L,
                    "User history recorded by agent for user ID " + userId);
        }

        return user;
    }
}
