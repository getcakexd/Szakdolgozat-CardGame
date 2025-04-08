package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.FriendshipController;
import hu.benkototh.cardgame.backend.rest.Data.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
public class FriendshipRestService {

    @Autowired
    private FriendshipController friendshipController;

    @GetMapping("/list")
    public ResponseEntity<List<User>> getFriends(@RequestParam long userId) {
        List<User> friends = friendshipController.getFriends(userId);
        
        if (friends == null) {
            return ResponseEntity.status(404).body(null);
        }
        
        return ResponseEntity.ok(friends);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, String>> removeFriend(@RequestParam long userId, @RequestParam long friendId) {
        Map<String, String> response = new HashMap<>();
        
        boolean removed = friendshipController.removeFriend(userId, friendId);
        
        if (!removed) {
            response.put("message", "User or friend not found.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "Friendship removed.");
        return ResponseEntity.ok(response);
    }
}
