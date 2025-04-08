package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.Friendship;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IFriendshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class FriendshipController {

    @Autowired
    private IFriendshipRepository friendshipRepository;

    @Lazy
    @Autowired
    private UserController userController;

    public List<User> getFriends(long userId) {
        User user = userController.getUser(userId);
        
        if (user == null) {
            return null;
        }

        return friendshipRepository.findAll().stream()
                .filter(friendship -> friendship.getUser1().equals(user) || friendship.getUser2().equals(user))
                .map(friendship -> friendship.getUser1().equals(user) ? friendship.getUser2() : friendship.getUser1())
                .collect(Collectors.toList());
    }

    public boolean removeFriend(long userId, long friendId) {
        User user = userController.getUser(userId);
        User friend = userController.getUser(friendId);

        if (user == null || friend == null) {
            return false;
        }

        Optional<Friendship> friendship = friendshipRepository.findAll().stream()
                .filter(f ->
                        (f.getUser1().equals(user) && f.getUser2().equals(friend)) ||
                        (f.getUser1().equals(friend) && f.getUser2().equals(user))
                ).findFirst();

        if (friendship.isEmpty()) {
            return false;
        }
        
        friendshipRepository.delete(friendship.get());
        return true;
    }
    
    public Friendship createFriendship(User user1, User user2) {
        Friendship friendship = new Friendship(user1, user2);
        return friendshipRepository.save(friendship);
    }
    
    public boolean friendshipExists(User user1, User user2) {
        return friendshipRepository.findAll().stream()
                .anyMatch(friendship ->
                        (friendship.getUser1().equals(user1) && friendship.getUser2().equals(user2)) ||
                        (friendship.getUser1().equals(user2) && friendship.getUser2().equals(user1))
                );
    }
    
    public void deleteFriendshipsByUser(User user) {
        List<Friendship> friendships = friendshipRepository.findAll().stream()
                .filter(friendship ->
                        friendship.getUser1().equals(user) ||
                        friendship.getUser2().equals(user)
                )
                .toList();
        
        friendshipRepository.deleteAll(friendships);
    }
}
