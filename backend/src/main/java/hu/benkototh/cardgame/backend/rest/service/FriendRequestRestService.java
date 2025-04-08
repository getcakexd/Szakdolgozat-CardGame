package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.FriendRequestController;
import hu.benkototh.cardgame.backend.rest.Data.FriendRequest;
import hu.benkototh.cardgame.backend.rest.controller.FriendshipController;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends/request")
public class FriendRequestRestService {

    @Autowired
    private FriendRequestController friendRequestController;
    @Autowired
    private UserController userController;
    @Autowired
    private FriendshipController friendshipController;

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendFriendRequest(@RequestParam long senderId, @RequestParam String receiverUsername) {
        Map<String, String> response = new HashMap<>();
        
        FriendRequest request = friendRequestController.sendFriendRequest(senderId, receiverUsername);
        
        if (request == null) {
            if (userController.findByUsername(receiverUsername) == null) {
                response.put("message", "Receiver not found.");
                return ResponseEntity.status(404).body(response);
            }
            
            if (senderId == userController.findByUsername(receiverUsername).getId()) {
                response.put("message", "You cannot send friend request to yourself.");
                return ResponseEntity.status(400).body(response);
            }
            
            if (friendRequestController.requestExists(
                    userController.findByUsername(receiverUsername),
                    userController.findByUsername(receiverUsername))) {
                response.put("message", "Friend request already sent.");
                return ResponseEntity.status(400).body(response);
            }
            
            if (friendshipController.friendshipExists(
                    userController.findByUsername(receiverUsername),
                    userController.findByUsername(receiverUsername))) {
                response.put("message", "You are already friends.");
                return ResponseEntity.status(400).body(response);
            }
            
            response.put("message", "Sender not found.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "Friend request sent.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<FriendRequest>> getPendingRequests(@RequestParam long userId) {
        return ResponseEntity.ok(friendRequestController.getPendingRequests(userId));
    }

    @GetMapping("/sent")
    public ResponseEntity<List<FriendRequest>> getSentRequests(@RequestParam long userId) {
        return ResponseEntity.ok(friendRequestController.getSentRequests(userId));
    }

    @PostMapping("/accept")
    public ResponseEntity<Map<String, String>> acceptFriendRequest(@RequestParam long requestId) {
        Map<String, String> response = new HashMap<>();
        
        FriendRequest request = friendRequestController.acceptFriendRequest(requestId);
        
        if (request == null) {
            response.put("message", "Friend request not found.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "Friend request accepted.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/decline")
    public ResponseEntity<Map<String, String>> declineFriendRequest(@RequestParam long requestId) {
        Map<String, String> response = new HashMap<>();
        
        FriendRequest request = friendRequestController.declineFriendRequest(requestId);
        
        if (request == null) {
            response.put("message", "Friend request not found.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "Friend request declined.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<Map<String, String>> cancelFriendRequest(@RequestParam long requestId) {
        Map<String, String> response = new HashMap<>();
        
        boolean canceled = friendRequestController.cancelFriendRequest(requestId);
        
        if (!canceled) {
            response.put("message", "Friend request not found.");
            return ResponseEntity.status(404).body(response);
        }

        response.put("message", "Friend request cancelled.");
        return ResponseEntity.ok(response);
    }
}
