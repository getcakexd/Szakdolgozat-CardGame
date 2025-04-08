package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.Data.UserHistory;
import hu.benkototh.cardgame.backend.rest.repository.IUserHistoryRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Controller
public class AgentController {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IUserHistoryRepository userHistoryRepository;

    public User unlockUser(long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        return userRepository.save(user);
    }

    public User modifyUserData(Map<String, Object> userData) {
        if (!userData.containsKey("userId")) {
            return null;
        }

        long userId = Long.parseLong(userData.get("userId").toString());
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();
        String previousUsername = null;
        String previousEmail = null;
        boolean modified = false;

        if (userData.containsKey("username") && !userData.get("username").equals(user.getUsername())) {
            String newUsername = userData.get("username").toString();

            if (userExistsByUsername(newUsername)) {
                return null;
            }

            previousUsername = user.getUsername();
            user.setUsername(newUsername);
            modified = true;
        }

        if (userData.containsKey("email") && !userData.get("email").equals(user.getEmail())) {
            String newEmail = userData.get("email").toString();

            if (userExistsByEmail(newEmail)) {
                return null;
            }

            previousEmail = user.getEmail();
            user.setEmail(newEmail);
            modified = true;
        }

        if (modified) {
            userRepository.save(user);

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

    public boolean userExistsByUsername(String username) {
        return userRepository.findAll().stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }

    public boolean userExistsByEmail(String email) {
        return userRepository.findAll().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }
}
