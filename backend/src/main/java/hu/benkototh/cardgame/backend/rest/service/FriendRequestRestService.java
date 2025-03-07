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
@RequestMapping("/api/friends")
public class FriendRequestRestService {

    @Autowired
    private IFriendRequestRepository friendRequestRepository;

    @Autowired
    private IFriendshipRepository friendshipRepository;

    @Autowired
    private IUserRepository userRepository;

    @PostMapping("/send")
    public Map<String, String> sendFriendRequest(@RequestParam Long senderId, @RequestParam Long receiverId) {
        Map<String, String> response = new HashMap<>();

        if (senderId.equals(receiverId)) {
            response.put("status", "error");
            response.put("message", "You cannot send a friend request to yourself.");
            return response;
        }

        Optional<User> senderOpt = userRepository.findById(senderId);
        Optional<User> receiverOpt = userRepository.findById(receiverId);

        if (senderOpt.isEmpty() || receiverOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "One or both users do not exist.");
            return response;
        }

        if (existsBySenderAndReceiver(senderOpt.get(), receiverOpt.get())) {
            response.put("status", "error");
            response.put("message", "Friend request already sent.");
            return response;
        }

        FriendRequest friendRequest = new FriendRequest(senderOpt.get(), receiverOpt.get());
        friendRequestRepository.save(friendRequest);
        response.put("status", "ok");
        response.put("message", "Friend request sent.");
        return response;
    }

    @GetMapping("/requests")
    public List<FriendRequest> getPendingRequests(@RequestParam Long userId) {
        return findByReceiverId(userId);
    }

    @PostMapping("/accept")
    public Map<String, String> acceptFriendRequest(@RequestParam Long requestId) {
        Map<String, String> response = new HashMap<>();
        Optional<FriendRequest> requestOpt = friendRequestRepository.findById(requestId);

        if (requestOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Friend request not found.");
            return response;
        }

        FriendRequest request = requestOpt.get();
        Friendship friendship = new Friendship(request.getSender(), request.getReceiver());
        friendshipRepository.save(friendship);
        friendRequestRepository.delete(request);
        response.put("status", "ok");
        response.put("message", "Friend request accepted.");
        return response;
    }

    @DeleteMapping("/decline")
    public Map<String, String> declineFriendRequest(@RequestParam Long requestId) {
        Map<String, String> response = new HashMap<>();
        Optional<FriendRequest> requestOpt = friendRequestRepository.findById(requestId);

        if (requestOpt.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Friend request not found.");
            return response;
        }

        friendRequestRepository.delete(requestOpt.get());
        response.put("status", "ok");
        response.put("message", "Friend request declined.");
        return response;
    }

    private boolean existsBySenderAndReceiver(User sender, User reciver) {
        return friendRequestRepository.findAll().stream()
                .anyMatch(request -> request.getSender().equals(sender) && request.getReceiver().equals(reciver));
    }

    private List<FriendRequest> findByReceiverId(Long userId) {
        return friendRequestRepository.findAll().stream()
                .filter(request -> request.getReceiver().getId() == userId)
                .toList();
    }
}
