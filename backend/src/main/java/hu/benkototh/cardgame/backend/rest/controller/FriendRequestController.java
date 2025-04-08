package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.FriendRequest;
import hu.benkototh.cardgame.backend.rest.Data.Friendship;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IFriendRequestRepository;
import hu.benkototh.cardgame.backend.rest.repository.IFriendshipRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
public class FriendRequestController {

    @Autowired
    private IFriendRequestRepository friendRequestRepository;

    @Autowired
    private IFriendshipRepository friendshipRepository;

    @Autowired
    private IUserRepository userRepository;

    public FriendRequest sendFriendRequest(long senderId, String receiverUsername) {
        Optional<User> senderOpt = userRepository.findById(senderId);
        Optional<User> receiverOpt = Optional.ofNullable(findByUsername(receiverUsername));

        if (receiverOpt.isEmpty() || senderOpt.isEmpty()) {
            return null;
        }

        User sender = senderOpt.get();
        User receiver = receiverOpt.get();

        if (senderId == receiver.getId()) {
            return null;
        }

        if (requestExists(sender, receiver)) {
            return null;
        }

        if (friendshipExists(sender, receiver)) {
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
        Friendship friendship = new Friendship(request.getSender(), request.getReceiver());
        friendRequestRepository.save(request);
        friendshipRepository.save(friendship);
        return request;
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

    public boolean friendshipExists(User user1, User user2) {
        return friendshipRepository.findAll().stream()
                .anyMatch(friendship ->
                        (friendship.getUser1().equals(user1) && friendship.getUser2().equals(user2)) ||
                        (friendship.getUser1().equals(user2) && friendship.getUser2().equals(user1))
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

    public User findByUsername(String username) {
        return userRepository.findAll().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }
}
