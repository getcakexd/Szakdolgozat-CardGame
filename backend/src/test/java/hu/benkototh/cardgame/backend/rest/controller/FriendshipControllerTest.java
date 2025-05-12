package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.Friendship;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IFriendshipRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipControllerTest {

    @Mock
    private IFriendshipRepository friendshipRepository;

    @Mock
    private UserController userController;

    @Mock
    private AuditLogController auditLogController;

    @InjectMocks
    private FriendshipController friendshipController;

    @Test
    void testGetFriendsSuccess() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        User friend1 = new User();
        friend1.setId(2L);
        friend1.setUsername("friend1");

        User friend2 = new User();
        friend2.setId(3L);
        friend2.setUsername("friend2");

        Friendship friendship1 = new Friendship(user, friend1);
        Friendship friendship2 = new Friendship(friend2, user);

        List<Friendship> friendships = new ArrayList<>();
        friendships.add(friendship1);
        friendships.add(friendship2);

        when(userController.getUser(1L)).thenReturn(user);
        when(friendshipRepository.findAll()).thenReturn(friendships);

        List<User> result = friendshipController.getFriends(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(friend1));
        assertTrue(result.contains(friend2));
    }

    @Test
    void testGetFriendsUserNotFound() {
        when(userController.getUser(1L)).thenReturn(null);

        List<User> result = friendshipController.getFriends(1L);

        assertNull(result);
    }

    @Test
    void testGetFriendsNoFriends() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userController.getUser(1L)).thenReturn(user);
        when(friendshipRepository.findAll()).thenReturn(new ArrayList<>());

        List<User> result = friendshipController.getFriends(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRemoveFriendSuccess() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        User friend = new User();
        friend.setId(2L);
        friend.setUsername("friend");

        Friendship friendship = new Friendship(user, friend);

        when(userController.getUser(1L)).thenReturn(user);
        when(userController.getUser(2L)).thenReturn(friend);
        when(friendshipRepository.findAll()).thenReturn(List.of(friendship));

        boolean result = friendshipController.removeFriend(1L, 2L);

        assertTrue(result);
        verify(friendshipRepository).delete(friendship);
        verify(auditLogController).logAction(eq("FRIEND_REMOVED"), eq(1L), anyString());
    }

    @Test
    void testRemoveFriendUserNotFound() {
        when(userController.getUser(1L)).thenReturn(null);

        boolean result = friendshipController.removeFriend(1L, 2L);

        assertFalse(result);
        verify(friendshipRepository, never()).delete(any());
    }

    @Test
    void testRemoveFriendFriendNotFound() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userController.getUser(1L)).thenReturn(user);
        when(userController.getUser(2L)).thenReturn(null);

        boolean result = friendshipController.removeFriend(1L, 2L);

        assertFalse(result);
        verify(friendshipRepository, never()).delete(any());
    }

    @Test
    void testRemoveFriendFriendshipNotFound() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        User friend = new User();
        friend.setId(2L);
        friend.setUsername("friend");

        when(userController.getUser(1L)).thenReturn(user);
        when(userController.getUser(2L)).thenReturn(friend);
        when(friendshipRepository.findAll()).thenReturn(new ArrayList<>());

        boolean result = friendshipController.removeFriend(1L, 2L);

        assertFalse(result);
        verify(friendshipRepository, never()).delete(any());
    }

    @Test
    void testCreateFriendship() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        Friendship friendship = new Friendship(user1, user2);
        friendship.setId(1L);

        when(friendshipRepository.save(any(Friendship.class))).thenReturn(friendship);

        Friendship result = friendshipController.createFriendship(user1, user2);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(user1, result.getUser1());
        assertEquals(user2, result.getUser2());
        verify(auditLogController).logAction(eq("FRIENDSHIP_CREATED"), eq(user1), anyString());
    }

    @Test
    void testFriendshipExistsTrue() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        Friendship friendship = new Friendship(user1, user2);

        when(friendshipRepository.findAll()).thenReturn(List.of(friendship));

        boolean result = friendshipController.friendshipExists(user1, user2);

        assertTrue(result);
    }

    @Test
    void testFriendshipExistsTrueReversed() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        Friendship friendship = new Friendship(user2, user1);

        when(friendshipRepository.findAll()).thenReturn(List.of(friendship));

        boolean result = friendshipController.friendshipExists(user1, user2);

        assertTrue(result);
    }

    @Test
    void testFriendshipExistsFalse() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        when(friendshipRepository.findAll()).thenReturn(new ArrayList<>());

        boolean result = friendshipController.friendshipExists(user1, user2);

        assertFalse(result);
    }
}