package hu.benkototh.cardgame.backend.rest.model;

import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class UserHistoryTest {

    @Test
    void testUserHistoryCreation() {
        UserHistory userHistory = new UserHistory();
        assertNotNull(userHistory);
    }

    @Test
    void testGettersAndSetters() {
        UserHistory userHistory = new UserHistory();
        
        long id = 1L;
        User user = new User();
        String previousUsername = "old_username";
        String previousEmail = "old.email@example.com";
        Date changedAt = new Date();
        String changedBy = "admin";
        
        userHistory.setId(id);
        userHistory.setUser(user);
        userHistory.setPreviousUsername(previousUsername);
        userHistory.setPreviousEmail(previousEmail);
        userHistory.setChangedAt(changedAt);
        userHistory.setChangedBy(changedBy);
        
        assertEquals(id, userHistory.getId());
        assertEquals(user, userHistory.getUser());
        assertEquals(previousUsername, userHistory.getPreviousUsername());
        assertEquals(previousEmail, userHistory.getPreviousEmail());
        assertEquals(changedAt, userHistory.getChangedAt());
        assertEquals(changedBy, userHistory.getChangedBy());
    }
}