package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.FriendRequest;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.controller.FriendRequestController;
import hu.benkototh.cardgame.backend.rest.controller.FriendshipController;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
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
class FriendRequestRestServiceTest {

    @Mock
    private FriendRequestController friendRequestController;

    @Mock
    private UserController userController;

    @Mock
    private FriendshipController friendshipController;

    @InjectMocks
    private FriendRequestRestService friendRequestRestService;

    @Test
    void testSendFriendRequestSuccess() {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");

        FriendRequest request = new FriendRequest(sender, receiver);

        when(friendRequestController.sendFriendRequest(1L, "receiver")).thenReturn(request);

        ResponseEntity<Map<String, String>> response = friendRequestRestService.sendFriendRequest(1L, "receiver");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Friend request sent.", response.getBody().get("message"));
    }

    @Test
    void testSendFriendRequestReceiverNotFound() {
        when(friendRequestController.sendFriendRequest(1L, "nonexistent")).thenReturn(null);
        when(userController.findByUsername("nonexistent")).thenReturn(null);

        ResponseEntity<Map<String, String>> response = friendRequestRestService.sendFriendRequest(1L, "nonexistent");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Receiver not found.", response.getBody().get("message"));
    }

    @Test
    void testSendFriendRequestToSelf() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user");

        when(friendRequestController.sendFriendRequest(1L, "user")).thenReturn(null);
        when(userController.findByUsername("user")).thenReturn(user);

        ResponseEntity<Map<String, String>> response = friendRequestRestService.sendFriendRequest(1L, "user");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("You cannot send friend request to yourself.", response.getBody().get("message"));
    }

    @Test
    void testSendFriendRequestAlreadySent() {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");

        when(friendRequestController.sendFriendRequest(1L, "receiver")).thenReturn(null);
        when(userController.findByUsername("receiver")).thenReturn(receiver);
        when(friendRequestController.requestExists(receiver, receiver)).thenReturn(true);

        ResponseEntity<Map<String, String>> response = friendRequestRestService.sendFriendRequest(1L, "receiver");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Friend request already sent.", response.getBody().get("message"));
    }

    @Test
    void testSendFriendRequestAlreadyFriends() {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");

        when(friendRequestController.sendFriendRequest(1L, "receiver")).thenReturn(null);
        when(userController.findByUsername("receiver")).thenReturn(receiver);
        when(friendRequestController.requestExists(receiver, receiver)).thenReturn(false);
        when(friendshipController.friendshipExists(receiver, receiver)).thenReturn(true);

        ResponseEntity<Map<String, String>> response = friendRequestRestService.sendFriendRequest(1L, "receiver");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("You are already friends.", response.getBody().get("message"));
    }

    @Test
    void testGetPendingRequests() {
        User receiver = new User();
        receiver.setId(1L);
        receiver.setUsername("receiver");

        User sender = new User();
        sender.setId(2L);
        sender.setUsername("sender");

        FriendRequest request = new FriendRequest(sender, receiver);
        request.setStatus("pending");

        List<FriendRequest> requests = new ArrayList<>();
        requests.add(request);

        when(friendRequestController.getPendingRequests(1L)).thenReturn(requests);

        ResponseEntity<List<FriendRequest>> response = friendRequestRestService.getPendingRequests(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("pending", response.getBody().get(0).getStatus());
    }

    @Test
    void testGetSentRequests() {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");

        FriendRequest request = new FriendRequest(sender, receiver);
        request.setStatus("pending");

        List<FriendRequest> requests = new ArrayList<>();
        requests.add(request);

        when(friendRequestController.getSentRequests(1L)).thenReturn(requests);

        ResponseEntity<List<FriendRequest>> response = friendRequestRestService.getSentRequests(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("pending", response.getBody().get(0).getStatus());
    }

    @Test
    void testAcceptFriendRequestSuccess() {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");

        FriendRequest request = new FriendRequest(sender, receiver);
        request.setStatus("accepted");

        when(friendRequestController.acceptFriendRequest(1L)).thenReturn(request);

        ResponseEntity<Map<String, String>> response = friendRequestRestService.acceptFriendRequest(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Friend request accepted.", response.getBody().get("message"));
    }

    @Test
    void testAcceptFriendRequestNotFound() {
        when(friendRequestController.acceptFriendRequest(1L)).thenReturn(null);

        ResponseEntity<Map<String, String>> response = friendRequestRestService.acceptFriendRequest(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Friend request not found.", response.getBody().get("message"));
    }

    @Test
    void testDeclineFriendRequestSuccess() {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");

        FriendRequest request = new FriendRequest(sender, receiver);
        request.setStatus("declined");

        when(friendRequestController.declineFriendRequest(1L)).thenReturn(request);

        ResponseEntity<Map<String, String>> response = friendRequestRestService.declineFriendRequest(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Friend request declined.", response.getBody().get("message"));
    }

    @Test
    void testDeclineFriendRequestNotFound() {
        when(friendRequestController.declineFriendRequest(1L)).thenReturn(null);

        ResponseEntity<Map<String, String>> response = friendRequestRestService.declineFriendRequest(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Friend request not found.", response.getBody().get("message"));
    }

    @Test
    void testCancelFriendRequestSuccess() {
        when(friendRequestController.cancelFriendRequest(1L)).thenReturn(true);

        ResponseEntity<Map<String, String>> response = friendRequestRestService.cancelFriendRequest(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Friend request cancelled.", response.getBody().get("message"));
    }

    @Test
    void testCancelFriendRequestNotFound() {
        when(friendRequestController.cancelFriendRequest(1L)).thenReturn(false);

        ResponseEntity<Map<String, String>> response = friendRequestRestService.cancelFriendRequest(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Friend request not found.", response.getBody().get("message"));
    }
}