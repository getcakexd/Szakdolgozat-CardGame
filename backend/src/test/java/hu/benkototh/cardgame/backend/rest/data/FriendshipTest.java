package hu.benkototh.cardgame.backend.rest.data;

import hu.benkototh.cardgame.backend.rest.Data.Friendship;
import hu.benkototh.cardgame.backend.rest.Data.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FriendshipTest {

    @Test
    void testFriendshipCreation() {
        Friendship friendship = new Friendship();
        assertNotNull(friendship);
    }

    @Test
    void testFriendshipCreationWithUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        Friendship friendship = new Friendship(user1, user2);
        
        assertNotNull(friendship);
        assertEquals(user1, friendship.getUser1());
        assertEquals(user2, friendship.getUser2());
    }

    @Test
    void testGettersAndSetters() {
        Friendship friendship = new Friendship();
        
        friendship.setId(1L);
        
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        friendship.setUser1(user1);
        
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        friendship.setUser2(user2);
        
        assertEquals(1L, friendship.getId());
        assertEquals(user1, friendship.getUser1());
        assertEquals(user2, friendship.getUser2());
    }
}