package hu.benkototh.cardgame.backend.rest.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FriendRequestTest {

    @Test
    void testFriendRequestCreation() {
        FriendRequest request = new FriendRequest();
        assertNotNull(request);
        assertEquals("pending", request.getStatus());
    }

    @Test
    void testFriendRequestCreationWithUsers() {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");

        FriendRequest request = new FriendRequest(sender, receiver);
        
        assertNotNull(request);
        assertEquals(sender, request.getSender());
        assertEquals(receiver, request.getReceiver());
        assertEquals("pending", request.getStatus());
    }

    @Test
    void testFriendRequestCreationWithUsersAndStatus() {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");

        FriendRequest request = new FriendRequest(sender, receiver, "accepted");
        
        assertNotNull(request);
        assertEquals(sender, request.getSender());
        assertEquals(receiver, request.getReceiver());
        assertEquals("accepted", request.getStatus());
    }

    @Test
    void testSetStatus() {
        FriendRequest request = new FriendRequest();
        assertEquals("pending", request.getStatus());
        
        request.setStatus("accepted");
        assertEquals("accepted", request.getStatus());
        
        request.setStatus("rejected");
        assertEquals("rejected", request.getStatus());
    }
}