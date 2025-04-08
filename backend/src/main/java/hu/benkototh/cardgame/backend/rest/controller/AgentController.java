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

    public User unlockUser(long userId) {
        User user = userController.getUser(userId);
        
        if (user == null) {
            return null;
        }

        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        return userController.userRepository.save(user);
    }

    public User modifyUserData(Map<String, Object> userData) {
        if (!userData.containsKey("userId")) {
            return null;
        }

        long userId = Long.parseLong(userData.get("userId").toString());
        User user = userController.getUser(userId);

        if (user == null) {
            return null;
        }

        String previousUsername = null;
        String previousEmail = null;
        boolean modified = false;

        if (userData.containsKey("username") && !userData.get("username").equals(user.getUsername())) {
            String newUsername = userData.get("username").toString();

            if (userController.userExistsByUsername(newUsername)) {
                return null;
            }

            previousUsername = user.getUsername();
            user.setUsername(newUsername);
            modified = true;
        }

        if (userData.containsKey("email") && !userData.get("email").equals(user.getEmail())) {
            String newEmail = userData.get("email").toString();

            if (userController.userExistsByEmail(newEmail)) {
                return null;
            }

            previousEmail = user.getEmail();
            user.setEmail(newEmail);
            modified = true;
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
        }

        return user;
    }
}
