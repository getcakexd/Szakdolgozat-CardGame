package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Friendship;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IFriendRequestRepository;
import hu.benkototh.cardgame.backend.rest.repository.IFriendshipRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<User>> getFriends(@RequestParam long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        User user = userOpt.get();
        List<User> list = friendshipRepository.findAll().stream()
                .filter(friendship -> friendship.getUser1().equals(user) || friendship.getUser2().equals(user))
                .map(friendship -> friendship.getUser1().equals(user) ? friendship.getUser2() : friendship.getUser1())
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> removeFriend(@RequestParam long userId, @RequestParam long friendId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<User> friendOpt = userRepository.findById(friendId);

        if (userOpt.isEmpty() || friendOpt.isEmpty()) return ResponseEntity.status(404).body("User not found.");

        User user = userOpt.get();
        User friend = friendOpt.get();

        Optional<Friendship> friendship = friendshipRepository.findAll().stream()
                .filter(f ->
                        (f.getUser1().equals(user) && f.getUser2().equals(friend)) ||
                        (f.getUser1().equals(friend) && f.getUser2().equals(user))
                ).findFirst();

        if (friendship.isPresent()) {
            friendshipRepository.delete(friendship.get());
            return ResponseEntity.ok("Friend removed.");
        } else {
            return ResponseEntity.status(404).body("Friendship not found.");
        }

    }
}
