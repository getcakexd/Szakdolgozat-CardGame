package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.model.FriendRequest;
import hu.benkototh.cardgame.backend.rest.model.Friendship;
import hu.benkototh.cardgame.backend.rest.model.User;
import hu.benkototh.cardgame.backend.rest.repository.IFriendRequestRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendRequestControllerTest {

    @Mock
    private IFriendRequestRepository friendRequestRepository;

    @Mock
    private UserController userController;

    @Mock
    private FriendshipController friendshipController;

    @Mock
    private AuditLogController auditLogController;

    @InjectMocks
    private FriendRequestController friendRequestController;

    @Test
    void testSendFriendRequestSuccess() {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");

        FriendRequest request = new FriendRequest(sender, receiver);
        request.setStatus("pending");

        when(userController.getUser(1L)).thenReturn(sender);
        when(userController.findByUsername("receiver")).thenReturn(receiver);
        when(friendRequestRepository.save(any(FriendRequest.class))).thenReturn(request);
        when(friendshipController.friendshipExists(sender, receiver)).thenReturn(false);

        FriendRequest result = friendRequestController.sendFriendRequest(1L, "receiver");

        assertNotNull(result);
        assertEquals(sender, result.getSender());
        assertEquals(receiver, result.getReceiver());
        assertEquals("pending", result.getStatus());
        verify(auditLogController).logAction(eq("FRIEND_REQUEST_SENT"), eq(1L), anyString());
    }

    @Test
    void testSendFriendRequestSenderNotFound() {
        when(userController.getUser(1L)).thenReturn(null);

        FriendRequest result = friendRequestController.sendFriendRequest(1L, "receiver");

        assertNull(result);
    }

    @Test
    void testSendFriendRequestReceiverNotFound() {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        when(userController.getUser(1L)).thenReturn(sender);
        when(userController.findByUsername("receiver")).thenReturn(null);

        FriendRequest result = friendRequestController.sendFriendRequest(1L, "receiver");

        assertNull(result);
    }

    @Test
    void testSendFriendRequestToSelf() {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        when(userController.getUser(1L)).thenReturn(sender);
        when(userController.findByUsername("sender")).thenReturn(sender);

        FriendRequest result = friendRequestController.sendFriendRequest(1L, "sender");

        assertNull(result);
    }

    @Test
    void testSendFriendRequestAlreadyExists() {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");

        FriendRequest existingRequest = new FriendRequest(sender, receiver);
        existingRequest.setStatus("pending");
        List<FriendRequest> requests = new ArrayList<>();
        requests.add(existingRequest);

        when(userController.getUser(1L)).thenReturn(sender);
        when(userController.findByUsername("receiver")).thenReturn(receiver);
        when(friendRequestRepository.findAll()).thenReturn(requests);

        FriendRequest result = friendRequestController.sendFriendRequest(1L, "receiver");

        assertNull(result);
    }

    @Test
    void testSendFriendRequestAlreadyFriends() {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");

        when(userController.getUser(1L)).thenReturn(sender);
        when(userController.findByUsername("receiver")).thenReturn(receiver);
        when(friendshipController.friendshipExists(sender, receiver)).thenReturn(true);

        FriendRequest result = friendRequestController.sendFriendRequest(1L, "receiver");

        assertNull(result);
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

        when(friendRequestRepository.findAll()).thenReturn(requests);

        List<FriendRequest> result = friendRequestController.getPendingRequests(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sender, result.get(0).getSender());
        assertEquals(receiver, result.get(0).getReceiver());
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

        when(friendRequestRepository.findAll()).thenReturn(requests);

        List<FriendRequest> result = friendRequestController.getSentRequests(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sender, result.get(0).getSender());
        assertEquals(receiver, result.get(0).getReceiver());
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
        request.setStatus("pending");

        when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(friendRequestRepository.save(any(FriendRequest.class))).thenReturn(request);
        when(friendshipController.createFriendship(sender, receiver)).thenReturn(new Friendship(sender, receiver));

        FriendRequest result = friendRequestController.acceptFriendRequest(1L);

        assertNotNull(result);
        assertEquals("accepted", result.getStatus());
        verify(friendshipController).createFriendship(sender, receiver);
        verify(auditLogController).logAction(eq("FRIEND_REQUEST_ACCEPTED"), eq(2L), anyString());
    }

    @Test
    void testAcceptFriendRequestNotFound() {
        when(friendRequestRepository.findById(1L)).thenReturn(Optional.empty());

        FriendRequest result = friendRequestController.acceptFriendRequest(1L);

        assertNull(result);
        verify(friendshipController, never()).createFriendship(any(), any());
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
        request.setStatus("pending");

        when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(friendRequestRepository.save(any(FriendRequest.class))).thenReturn(request);

        FriendRequest result = friendRequestController.declineFriendRequest(1L);

        assertNotNull(result);
        assertEquals("declined", result.getStatus());
        verify(auditLogController).logAction(eq("FRIEND_REQUEST_DECLINED"), eq(2L), anyString());
    }

    @Test
    void testDeclineFriendRequestNotFound() {
        when(friendRequestRepository.findById(1L)).thenReturn(Optional.empty());

        FriendRequest result = friendRequestController.declineFriendRequest(1L);

        assertNull(result);
    }

    @Test
    void testCancelFriendRequestSuccess() {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");

        FriendRequest request = new FriendRequest(sender, receiver);
        request.setStatus("pending");

        when(friendRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        boolean result = friendRequestController.cancelFriendRequest(1L);

        assertTrue(result);
        verify(friendRequestRepository).delete(request);
        verify(auditLogController).logAction(eq("FRIEND_REQUEST_CANCELED"), eq(1L), anyString());
    }

    @Test
    void testCancelFriendRequestNotFound() {
        when(friendRequestRepository.findById(1L)).thenReturn(Optional.empty());

        boolean result = friendRequestController.cancelFriendRequest(1L);

        assertFalse(result);
        verify(friendRequestRepository, never()).delete(any());
    }

    @Test
    void testRequestExistsTrue() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        FriendRequest request = new FriendRequest(user1, user2);
        request.setStatus("pending");

        when(friendRequestRepository.findAll()).thenReturn(List.of(request));

        boolean result = friendRequestController.requestExists(user1, user2);

        assertTrue(result);
    }

    @Test
    void testRequestExistsTrueReversed() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        FriendRequest request = new FriendRequest(user2, user1);
        request.setStatus("pending");

        when(friendRequestRepository.findAll()).thenReturn(List.of(request));

        boolean result = friendRequestController.requestExists(user1, user2);

        assertTrue(result);
    }

    @Test
    void testRequestExistsFalse() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        when(friendRequestRepository.findAll()).thenReturn(new ArrayList<>());

        boolean result = friendRequestController.requestExists(user1, user2);

        assertFalse(result);
    }

    @Test
    void testRequestExistsFalseNonPendingStatus() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        FriendRequest request = new FriendRequest(user1, user2);
        request.setStatus("accepted");

        when(friendRequestRepository.findAll()).thenReturn(List.of(request));

        boolean result = friendRequestController.requestExists(user1, user2);

        assertFalse(result);
    }

    @Test
    void testFindByReceiverId() {
        User receiver = new User();
        receiver.setId(1L);
        receiver.setUsername("receiver");

        User sender = new User();
        sender.setId(2L);
        sender.setUsername("sender");

        FriendRequest pendingRequest = new FriendRequest(sender, receiver);
        pendingRequest.setStatus("pending");

        FriendRequest acceptedRequest = new FriendRequest(sender, receiver);
        acceptedRequest.setStatus("accepted");

        List<FriendRequest> allRequests = new ArrayList<>();
        allRequests.add(pendingRequest);
        allRequests.add(acceptedRequest);

        when(friendRequestRepository.findAll()).thenReturn(allRequests);

        List<FriendRequest> result = friendRequestController.findByReceiverId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("pending", result.get(0).getStatus());
    }

    @Test
    void testFindBySenderId() {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");

        FriendRequest request1 = new FriendRequest(sender, receiver);
        request1.setStatus("pending");

        FriendRequest request2 = new FriendRequest(sender, receiver);
        request2.setStatus("accepted");

        List<FriendRequest> allRequests = new ArrayList<>();
        allRequests.add(request1);
        allRequests.add(request2);

        when(friendRequestRepository.findAll()).thenReturn(allRequests);

        List<FriendRequest> result = friendRequestController.findBySenderId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
    }
}