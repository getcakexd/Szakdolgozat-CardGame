package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.Friendship;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IFriendshipRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class FriendshipController {

    @Autowired
    private IFriendshipRepository friendshipRepository;

    @Autowired
    private IUserRepository userRepository;

    public List<User> getFriends(long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();
        return friendshipRepository.findAll().stream()
                .filter(friendship -> friendship.getUser1().equals(user) || friendship.getUser2().equals(user))
                .map(friendship -> friendship.getUser1().equals(user) ? friendship.getUser2() : friendship.getUser1())
                .collect(Collectors.toList());
    }

    public boolean removeFriend(long userId, long friendId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<User> friendOpt = userRepository.findById(friendId);

        if (userOpt.isEmpty() || friendOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        User friend = friendOpt.get();

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
}
