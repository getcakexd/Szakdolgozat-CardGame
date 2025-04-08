package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.*;
import hu.benkototh.cardgame.backend.rest.repository.*;
import hu.benkototh.cardgame.backend.rest.service.ClubRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class UserController {

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
            return null;
        }

        User existingUser = foundUser.get();

        if (existingUser.isLocked()) {
            return null;
        }

        if (passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            existingUser.setFailedLoginAttempts(0);
            return userRepository.save(existingUser);
        } else {
            existingUser.setFailedLoginAttempts(existingUser.getFailedLoginAttempts() + 1);

            if (existingUser.getFailedLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
                existingUser.setLocked(true);
            }

            userRepository.save(existingUser);
            return null;
        }
    }

    public User createUser(User user) {
        if (userExistsByUsername(user.getUsername()) || userExistsByEmail(user.getEmail())) {
            return null;
        }
        
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setRole("ROLE_USER");
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        return userRepository.save(user);
    }

    public User updateUsername(long userId, String newUsername) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty() || userExistsByUsername(newUsername)) {
            return null;
        }
        
        User user = userOptional.get();
        String oldUsername = user.getUsername();
        user.setUsername(newUsername);
        User updatedUser = userRepository.save(user);
        
        saveUserHistory(user, oldUsername, null, "self");
        
        return updatedUser;
    }

    public User updateEmail(long userId, String newEmail) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty() || userExistsByEmail(newEmail)) {
            return null;
        }
        
        User user = userOptional.get();
        String oldEmail = user.getEmail();
        user.setEmail(newEmail);
        User updatedUser = userRepository.save(user);
        
        saveUserHistory(user, null, oldEmail, "self");
        
        return updatedUser;
    }

    public User updatePassword(long userId, String currentPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return null;
        }
        
        User user = userOptional.get();
        
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            return null;
        }
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return null;
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    public boolean deleteUser(long userId, String password) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return false;
        }
        
        User user = userOptional.get();
        
        if (!passwordEncoder.matches(password, user.getPassword()) && !password.equals(user.getPassword())) {
            return false;
        }
        
        friendRequestRepository.deleteAll(getFriendRequests(user));
        friendshipRepository.deleteAll(getFriendships(user));
        messageRepository.deleteAll(getMessages(user));
        clubMemberRepository.delete(getClubMember(user));
        clubMessageRepository.deleteAll(getClubMessages(user));
        clubInviteRepository.deleteAll(getInvites(user));
        userRepository.delete(user);
        
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
        return userRepository.findById(user.getId())
                .map(userAuth -> passwordEncoder.matches(user.getPassword(), userAuth.getPassword()))
                .orElse(false);
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
