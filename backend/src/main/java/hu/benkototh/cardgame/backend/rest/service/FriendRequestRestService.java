package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.Friendship;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.Data.FriendRequest;
import hu.benkototh.cardgame.backend.rest.repository.IFriendRequestRepository;
import hu.benkototh.cardgame.backend.rest.repository.IFriendshipRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Map<String, String> sendFriendRequest(@RequestParam String senderId, @RequestParam String receiverUsername) {
        Map<String, String> response = new HashMap<>();
        Long senderIdLong = Long.parseLong(senderId);
        Optional<User> senderOpt = userRepository.findById(senderIdLong);
        Optional<User> receiverOpt = Optional.ofNullable(findByUsername(receiverUsername));


        if(receiverOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Receiver does not exist.");
            return response;
        }

        if (senderOpt.isEmpty() ) {
            response.put("status", "error");
            response.put("message", "Error: Failed to send friend request.");
            return response;
        }

        if (senderIdLong.equals(receiverOpt.get().getId())) {
            response.put("status", "error");
            response.put("message", "You cannot send a friend request to yourself.");
            return response;
        }

        if (requestExists(senderOpt.get(), receiverOpt.get())) {
            response.put("status", "error");
            response.put("message", "Friend request already exists.");
            return response;
        }

        if (friendshipExists(senderOpt.get(), receiverOpt.get())) {
            response.put("status", "error");
            response.put("message", "You are already friends.");
            return response;
        }

        FriendRequest friendRequest = new FriendRequest(senderOpt.get(), receiverOpt.get());
        friendRequestRepository.save(friendRequest);
        response.put("status", "ok");
        response.put("message", "Friend request sent.");
        return response;
    }


    @GetMapping("/requests")
    public List<FriendRequest> getPendingRequests(@RequestParam String userId) {
        Long userIdLong = Long.parseLong(userId);
        return findByReceiverId(userIdLong);
    }

    @GetMapping("/sent")
    public List<FriendRequest> getSentRequests(@RequestParam String userId) {
        Long userIdLong = Long.parseLong(userId);
        return findBySenderId(userIdLong);
    }

    @PostMapping("/accept")
    public Map<String, String> acceptFriendRequest(@RequestParam String requestId) {
        Map<String, String> response = new HashMap<>();
        Long requestIdLong = Long.parseLong(requestId);
        Optional<FriendRequest> requestOpt = friendRequestRepository.findById(requestIdLong);

        if (requestOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Friend request not found.");
            return response;
        }

        FriendRequest request = requestOpt.get();
        request.setStatus("accepted");
        Friendship friendship = new Friendship(request.getSender(), request.getReceiver());
        friendRequestRepository.save(request);
        friendshipRepository.save(friendship);
        response.put("status", "ok");
        response.put("message", "Friend request accepted.");
        return response;
    }

    @DeleteMapping("/decline")
    public Map<String, String> declineFriendRequest(@RequestParam String requestId) {
        Map<String, String> response = new HashMap<>();
        Long requestIdLong = Long.parseLong(requestId);
        Optional<FriendRequest> requestOpt = friendRequestRepository.findById(requestIdLong);

        if (requestOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Friend request not found.");
            return response;
        }

        FriendRequest request = requestOpt.get();
        request.setStatus("declined");
        friendRequestRepository.save(request);
        response.put("status", "ok");
        response.put("message", "Friend request declined.");
        return response;
    }

    @DeleteMapping("/cancel")
    public Map<String, String> cancelFriendRequest(@RequestParam String requestId) {
        Map<String, String> response = new HashMap<>();
        Long requestIdLong = Long.parseLong(requestId);
        Optional<FriendRequest> requestOpt = friendRequestRepository.findById(requestIdLong);

        if (requestOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Friend request not found.");
            return response;
        }

        FriendRequest request = requestOpt.get();
        friendRequestRepository.delete(request);
        response.put("status", "ok");
        response.put("message", "Friend request canceled.");
        return response;
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
