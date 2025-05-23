package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.model.User;
import hu.benkototh.cardgame.backend.rest.controller.AgentController;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentRestServiceTest {

    @Mock
    private AgentController agentController;

    @Mock
    private UserController userController;

    @InjectMocks
    private AgentRestService agentRestService;

    private User testUser;
    private Map<String, Object> userData;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        userData = new HashMap<>();
        userData.put("userId", "1");
        userData.put("username", "newusername");
        userData.put("email", "newemail@example.com");
    }

    @Test
    void testUnlockUserSuccess() {
        when(agentController.unlockUser(1L)).thenReturn(testUser);

        ResponseEntity<Map<String, String>> response = agentRestService.unlockUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User unlocked successfully.", response.getBody().get("message"));
    }

    @Test
    void testUnlockUserNotFound() {
        when(agentController.unlockUser(1L)).thenReturn(null);

        ResponseEntity<Map<String, String>> response = agentRestService.unlockUser(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found.", response.getBody().get("message"));
    }

    @Test
    void testModifyUserDataSuccess() {
        when(agentController.modifyUserData(anyMap())).thenReturn(testUser);

        ResponseEntity<Map<String, String>> response = agentRestService.modifyUserData(userData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User data modified successfully.", response.getBody().get("message"));
    }

    @Test
    void testModifyUserDataNoUserId() {
        Map<String, Object> invalidData = new HashMap<>();

        when(agentController.modifyUserData(anyMap())).thenReturn(null);

        ResponseEntity<Map<String, String>> response = agentRestService.modifyUserData(invalidData);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User ID is required.", response.getBody().get("message"));
    }

    @Test
    void testModifyUserDataUsernameExists() {
        when(agentController.modifyUserData(anyMap())).thenReturn(null);
        when(userController.userExistsByUsername("newusername")).thenReturn(true);

        ResponseEntity<Map<String, String>> response = agentRestService.modifyUserData(userData);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username already in use.", response.getBody().get("message"));
    }

    @Test
    void testModifyUserDataEmailExists() {
        when(agentController.modifyUserData(anyMap())).thenReturn(null);
        when(userController.userExistsByUsername("newusername")).thenReturn(false);
        when(userController.userExistsByEmail("newemail@example.com")).thenReturn(true);

        ResponseEntity<Map<String, String>> response = agentRestService.modifyUserData(userData);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email already in use.", response.getBody().get("message"));
    }

    @Test
    void testModifyUserDataUserNotFound() {
        when(agentController.modifyUserData(anyMap())).thenReturn(null);
        when(userController.userExistsByUsername("newusername")).thenReturn(false);
        when(userController.userExistsByEmail("newemail@example.com")).thenReturn(false);

        ResponseEntity<Map<String, String>> response = agentRestService.modifyUserData(userData);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found.", response.getBody().get("message"));
    }
}