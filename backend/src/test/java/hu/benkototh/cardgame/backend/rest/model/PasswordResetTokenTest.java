package hu.benkototh.cardgame.backend.rest.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class PasswordResetTokenTest {

    @Test
    void testPasswordResetTokenCreation() {
        PasswordResetToken token = new PasswordResetToken();
        assertNotNull(token);
    }

    @Test
    void testPasswordResetTokenCreationWithParameters() {
        String tokenValue = "test-token";
        User user = new User();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(24);
        
        PasswordResetToken token = new PasswordResetToken(tokenValue, user, expiryDate);
        
        assertNotNull(token);
        assertEquals(tokenValue, token.getToken());
        assertEquals(user, token.getUser());
        assertEquals(expiryDate, token.getExpiryDate());
    }

    @Test
    void testGettersAndSetters() {
        PasswordResetToken token = new PasswordResetToken();
        
        Long id = 1L;
        String tokenValue = "test-token";
        User user = new User();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(24);
        
        token.setId(id);
        token.setToken(tokenValue);
        token.setUser(user);
        token.setExpiryDate(expiryDate);
        
        assertEquals(id, token.getId());
        assertEquals(tokenValue, token.getToken());
        assertEquals(user, token.getUser());
        assertEquals(expiryDate, token.getExpiryDate());
    }

    @Test
    void testIsExpiredWithFutureDate() {
        LocalDateTime futureDate = LocalDateTime.now().plusHours(24);
        PasswordResetToken token = new PasswordResetToken("test-token", new User(), futureDate);
        
        assertFalse(token.isExpired());
    }

    @Test
    void testIsExpiredWithPastDate() {
        LocalDateTime pastDate = LocalDateTime.now().minusHours(24);
        PasswordResetToken token = new PasswordResetToken("test-token", new User(), pastDate);
        
        assertTrue(token.isExpired());
    }
}