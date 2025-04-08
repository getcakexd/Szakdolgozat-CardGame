package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.FriendRequest;
import hu.benkototh.cardgame.backend.rest.Data.Friendship;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IFriendRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
public class FriendRequestController {

    @Autowired
    private IFriendRequestRepository friendRequestRepository;
    
    @Lazy
    @Autowired
    private UserController userController;
    
    @Lazy
    @Autowired
    private FriendshipController friendshipController;

    public FriendRequest sendFriendRequest(long senderId, String receiverUsername) {
        User sender = userController.getUser(senderId);
        User receiver = userController.findByUsername(receiverUsername);

        if (receiver == null || sender == null) {
            return null;
        }

        if (senderId == receiver.getId()) {
            return null;
        }

        if (requestExists(sender, receiver)) {
            return null;
        }

        if (friendshipController.friendshipExists(sender, receiver)) {
            return null;
        }

        FriendRequest friendRequest = new FriendRequest(sender, receiver);
        return friendRequestRepository.save(friendRequest);
    }

    public List<FriendRequest> getPendingRequests(long userId) {
        return findByReceiverId(userId);
    }

    public List<FriendRequest> getSentRequests(long userId) {
        return findBySenderId(userId);
    }

    public FriendRequest acceptFriendRequest(long requestId) {
        Optional<FriendRequest> requestOpt = friendRequestRepository.findById(requestId);
        
        if (requestOpt.isEmpty()) {
            return null;
        }

        FriendRequest request = requestOpt.get();
        request.setStatus("accepted");

        friendshipController.createFriendship(request.getSender(), request.getReceiver());
        
        return friendRequestRepository.save(request);
    }

    public FriendRequest declineFriendRequest(long requestId) {
        Optional<FriendRequest> requestOpt = friendRequestRepository.findById(requestId);
        
        if (requestOpt.isEmpty()) {
            return null;
        }

        FriendRequest request = requestOpt.get();
        request.setStatus("declined");
        return friendRequestRepository.save(request);
    }

    public boolean cancelFriendRequest(long requestId) {
        Optional<FriendRequest> requestOpt = friendRequestRepository.findById(requestId);
        
        if (requestOpt.isEmpty()) {
            return false;
        }

        FriendRequest request = requestOpt.get();
        friendRequestRepository.delete(request);
        return true;
    }

    public boolean requestExists(User user1, User user2) {
        return friendRequestRepository.findAll().stream()
                .anyMatch(request ->
                        request.getStatus().equals("pending") && (
                            (request.getSender().equals(user1) && request.getReceiver().equals(user2)) ||
                            (request.getSender().equals(user2) && request.getReceiver().equals(user1))
                        )
                );
    }

    public List<FriendRequest> findByReceiverId(Long userId) {
        return friendRequestRepository.findAll().stream()
                .filter(request ->
                        request.getReceiver().getId() == userId && request.getStatus().equals("pending"))
                .toList();
    }

    public List<FriendRequest> findBySenderId(Long userId) {
        return friendRequestRepository.findAll().stream()
                .filter(request -> request.getSender().getId() == userId)
                .sorted(Comparator.comparing(FriendRequest::getId).reversed())
                .toList();
    }
    
    public void deleteFriendRequestsByUser(User user) {
        List<FriendRequest> requests = friendRequestRepository.findAll().stream()
                .filter(request ->
                        request.getReceiver().getId() == user.getId() ||
                        request.getSender().getId() == user.getId()
                )
                .toList();
        
        friendRequestRepository.deleteAll(requests);
    }
}
