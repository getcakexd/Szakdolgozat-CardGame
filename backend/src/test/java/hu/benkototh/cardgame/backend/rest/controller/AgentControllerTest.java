package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.model.User;
import hu.benkototh.cardgame.backend.rest.repository.IUserHistoryRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentControllerTest {

    @Mock
    private UserController userController;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IUserHistoryRepository userHistoryRepository;

    @Mock
    private AuditLogController auditLogController;

    @InjectMocks
    private AgentController agentController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setLocked(true);
        testUser.setFailedLoginAttempts(5);
    }
    
    @Test
    void testUnlockUserNotFound() {
        when(userController.getUser(1L)).thenReturn(null);

        User result = agentController.unlockUser(1L);

        assertNull(result);
        verify(userRepository, never()).save(any(User.class));
        verify(auditLogController).logAction(eq("USER_UNLOCK_FAILED"), eq(0L), anyString());
    }

    @Test
    void testModifyUserDataNoUserId() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "newusername");

        User result = agentController.modifyUserData(userData);

        assertNull(result);
        verify(userRepository, never()).save(any(User.class));
        verify(auditLogController).logAction(eq("USER_MODIFY_FAILED"), eq(0L), anyString());
    }

    @Test
    void testModifyUserDataUserNotFound() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", "1");

        when(userController.getUser(1L)).thenReturn(null);

        User result = agentController.modifyUserData(userData);

        assertNull(result);
        verify(userRepository, never()).save(any(User.class));
        verify(auditLogController).logAction(eq("USER_MODIFY_FAILED"), eq(0L), anyString());
    }

    @Test
    void testModifyUserDataUsernameExists() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", "1");
        userData.put("username", "existinguser");

        when(userController.getUser(1L)).thenReturn(testUser);
        when(userController.userExistsByUsername("existinguser")).thenReturn(true);

        User result = agentController.modifyUserData(userData);

        assertNull(result);
        verify(userRepository, never()).save(any(User.class));
        verify(auditLogController).logAction(eq("USER_MODIFY_FAILED"), eq(0L), anyString());
    }

    @Test
    void testModifyUserDataEmailExists() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", "1");
        userData.put("email", "existing@example.com");

        when(userController.getUser(1L)).thenReturn(testUser);
        when(userController.userExistsByEmail("existing@example.com")).thenReturn(true);

        User result = agentController.modifyUserData(userData);

        assertNull(result);
        verify(userRepository, never()).save(any(User.class));
        verify(auditLogController).logAction(eq("USER_MODIFY_FAILED"), eq(0L), anyString());
    }

    @Test
    void testModifyUserDataNoChanges() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", "1");
        userData.put("username", "testuser");
        userData.put("email", "test@example.com");

        when(userController.getUser(1L)).thenReturn(testUser);

        User result = agentController.modifyUserData(userData);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(userHistoryRepository, never()).save(any());
    }
}