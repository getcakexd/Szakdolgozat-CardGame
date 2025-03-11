package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Friendship;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IFriendRequestRepository;
import hu.benkototh.cardgame.backend.rest.repository.IFriendshipRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friends")
public class FriendshipRestService {

    @Autowired
    private IFriendshipRepository friendshipRepository;

    @Autowired
    private IUserRepository userRepository;

    @GetMapping("/list")
    public List<User> getFriends(@RequestParam String userId) {
        Long userIdLong = Long.parseLong(userId);
        Optional<User> userOpt = userRepository.findById(userIdLong);
        if (userOpt.isEmpty()) {
            return Collections.emptyList();
        }

        User user = userOpt.get();
        return friendshipRepository.findAll().stream()
                .filter(friendship -> friendship.getUser1().equals(user) || friendship.getUser2().equals(user))
                .map(friendship -> friendship.getUser1().equals(user) ? friendship.getUser2() : friendship.getUser1())
                .collect(Collectors.toList());
    }

    @DeleteMapping("/remove")
    public Map<String, String> removeFriend(@RequestParam String userId, @RequestParam String friendId) {
        Long userIdLong = Long.parseLong(userId);
        Long friendIdLong = Long.parseLong(friendId);
        Map<String, String> response = new HashMap<>();
        Optional<User> userOpt = userRepository.findById(userIdLong);
        Optional<User> friendOpt = userRepository.findById(friendIdLong);

        if (userOpt.isEmpty() || friendOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "One or both users do not exist.");
            return response;
        }

        User user = userOpt.get();
        User friend = friendOpt.get();

        Optional<Friendship> friendship = friendshipRepository.findAll().stream()
                .filter(f ->
                        (f.getUser1().equals(user) && f.getUser2().equals(friend)) ||
                        (f.getUser1().equals(friend) && f.getUser2().equals(user))
                ).findFirst();

        if (friendship.isPresent()) {
            friendshipRepository.delete(friendship.get());
            response.put("status", "ok");
            response.put("message", "Friend removed successfully.");
        } else {
            response.put("status", "error");
            response.put("message", "Friendship not found.");
        }

        return response;
    }
}
