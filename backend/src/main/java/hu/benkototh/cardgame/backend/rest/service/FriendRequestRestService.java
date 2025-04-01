package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Friendship;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.Data.FriendRequest;
import hu.benkototh.cardgame.backend.rest.repository.IFriendRequestRepository;
import hu.benkototh.cardgame.backend.rest.repository.IFriendshipRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/friends/request")
public class FriendRequestRestService {

    @Autowired
    private IFriendRequestRepository friendRequestRepository;

    @Autowired
    private IFriendshipRepository friendshipRepository;

    @Autowired
    private IUserRepository userRepository;

    @PostMapping("/send")
    public ResponseEntity<String> sendFriendRequest(@RequestParam long senderId, @RequestParam String receiverUsername) {
        Map<String, String> response = new HashMap<>();
        Optional<User> senderOpt = userRepository.findById(senderId);
        Optional<User> receiverOpt = Optional.ofNullable(findByUsername(receiverUsername));


        if(receiverOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Receiver not found.");
        }

        if (senderOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Sender not found.");
        }

        if (senderId == receiverOpt.get().getId()) {
            return ResponseEntity.status(400).body("You cannot send friend request to yourself.");
        }

        if (requestExists(senderOpt.get(), receiverOpt.get())) {
            return ResponseEntity.status(400).body("Friend request already sent.");
        }

        if (friendshipExists(senderOpt.get(), receiverOpt.get())) {
            return ResponseEntity.status(400).body("You are already friends.");
        }

        FriendRequest friendRequest = new FriendRequest(senderOpt.get(), receiverOpt.get());
        friendRequestRepository.save(friendRequest);
        return ResponseEntity.ok("Friend request sent.");
    }


    @GetMapping("/requests")
    public ResponseEntity<List<FriendRequest>> getPendingRequests(@RequestParam long userId) {
        return ResponseEntity.ok(findByReceiverId(userId));
    }

    @GetMapping("/sent")
    public ResponseEntity<List<FriendRequest>> getSentRequests(@RequestParam long userId) {
        return ResponseEntity.ok(findBySenderId(userId));
    }

    @PostMapping("/accept")
    public ResponseEntity<String> acceptFriendRequest(@RequestParam long requestId) {
        Optional<FriendRequest> requestOpt = friendRequestRepository.findById(requestId);

        if (requestOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Friend request not found.");
        }

        FriendRequest request = requestOpt.get();
        request.setStatus("accepted");
        Friendship friendship = new Friendship(request.getSender(), request.getReceiver());
        friendRequestRepository.save(request);
        friendshipRepository.save(friendship);
        return ResponseEntity.ok("Friend request accepted.");
    }

    @DeleteMapping("/decline")
    public ResponseEntity<String> declineFriendRequest(@RequestParam long requestId) {
        Optional<FriendRequest> requestOpt = friendRequestRepository.findById(requestId);

        if (requestOpt.isEmpty()) return ResponseEntity.status(404).body("Friend request not found.");

        FriendRequest request = requestOpt.get();
        request.setStatus("declined");
        friendRequestRepository.save(request);
        return ResponseEntity.ok("Friend request declined.");
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<String> cancelFriendRequest(@RequestParam long requestId) {
        Optional<FriendRequest> requestOpt = friendRequestRepository.findById(requestId);

        if (requestOpt.isEmpty()) return ResponseEntity.status(404).body("Friend request not found.");

        FriendRequest request = requestOpt.get();
        friendRequestRepository.delete(request);
        return ResponseEntity.ok("Friend request cancelled.");
    }

    private boolean requestExists(User user1, User user2) {
        return friendRequestRepository.findAll().stream()
                .anyMatch(request ->
                        request.getStatus().equals("pending") && (
                            (request.getSender().equals(user1) && request.getReceiver().equals(user2)) ||
                            (request.getSender().equals(user2) && request.getReceiver().equals(user1))
                        )
                );
    }

    private boolean friendshipExists(User user1, User user2) {
        return friendshipRepository.findAll().stream()
                .anyMatch(friendship ->
                        (friendship.getUser1().equals(user1) && friendship.getUser2().equals(user2)) ||
                        (friendship.getUser1().equals(user2) && friendship.getUser2().equals(user1))
                );
    }


    private List<FriendRequest> findByReceiverId(Long userId) {
        return friendRequestRepository.findAll().stream()
                .filter(request ->
                        request.getReceiver().getId() == userId && request.getStatus().equals("pending"))
                .toList();
    }

    private List<FriendRequest> findBySenderId(Long userId) {
        return friendRequestRepository.findAll().stream()
                .filter(request ->
                        request.getSender().getId() == userId)
                .toList();
    }


    private User findByUsername(String username) {
        return userRepository.findAll().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst().
                orElse(null);
    }
}
