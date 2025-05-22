package hu.benkototh.cardgame.backend.rest.model;

import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserCreation() {
        User user = new User();
        assertNotNull(user);
    }

    @Test
    void testGettersAndSetters() {
        User user = new User();
        
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("hashedpassword");
        user.setEmail("test@example.com");
        user.setRole("ROLE_USER");
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        user.setVerified(true);
        user.setVerificationToken("token123");
        
        Date now = new Date();
        user.setVerificationTokenExpiry(now);
        
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("hashedpassword", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("ROLE_USER", user.getRole());
        assertFalse(user.isLocked());
        assertEquals(0, user.getFailedLoginAttempts());
        assertTrue(user.isVerified());
        assertEquals("token123", user.getVerificationToken());
        assertEquals(now, user.getVerificationTokenExpiry());
    }
    
    @Test
    void testDefaultValues() {
        User user = new User();
        assertFalse(user.isVerified());
    }
}