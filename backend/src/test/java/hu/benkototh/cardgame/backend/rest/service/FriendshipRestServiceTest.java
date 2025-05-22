package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.model.User;
import hu.benkototh.cardgame.backend.rest.controller.FriendshipController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipRestServiceTest {

    @Mock
    private FriendshipController friendshipController;

    @InjectMocks
    private FriendshipRestService friendshipRestService;

    @Test
    void testGetFriendsSuccess() {
        User friend1 = new User();
        friend1.setId(1L);
        friend1.setUsername("friend1");

        User friend2 = new User();
        friend2.setId(2L);
        friend2.setUsername("friend2");

        List<User> friends = new ArrayList<>();
        friends.add(friend1);
        friends.add(friend2);

        when(friendshipController.getFriends(1L)).thenReturn(friends);

        ResponseEntity<List<User>> response = friendshipRestService.getFriends(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("friend1", response.getBody().get(0).getUsername());
        assertEquals("friend2", response.getBody().get(1).getUsername());
    }

    @Test
    void testGetFriendsUserNotFound() {
        when(friendshipController.getFriends(1L)).thenReturn(null);

        ResponseEntity<List<User>> response = friendshipRestService.getFriends(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testRemoveFriendSuccess() {
        when(friendshipController.removeFriend(1L, 2L)).thenReturn(true);

        ResponseEntity<Map<String, String>> response = friendshipRestService.removeFriend(1L, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Friendship removed.", response.getBody().get("message"));
    }

    @Test
    void testRemoveFriendNotFound() {
        when(friendshipController.removeFriend(1L, 2L)).thenReturn(false);

        ResponseEntity<Map<String, String>> response = friendshipRestService.removeFriend(1L, 2L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User or friend not found.", response.getBody().get("message"));
    }
}