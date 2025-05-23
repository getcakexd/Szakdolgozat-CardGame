package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.model.GoogleAuthRequest;
import hu.benkototh.cardgame.backend.rest.model.User;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRestServiceTest {

    @Mock
    private UserController userController;

    @InjectMocks
    private UserRestService userRestService;

    @Test
    void testGetAllUsers() {
        List<User> users = new ArrayList<>();
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        users.add(user);
        
        when(userController.getAllUsers()).thenReturn(users);
        
        ResponseEntity<List<User>> response = userRestService.all();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("testuser", response.getBody().get(0).getUsername());
    }

    @Test
    void testGetUserFound() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        
        when(userController.getUser(1L)).thenReturn(user);
        
        ResponseEntity<User> response = userRestService.getUser(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("testuser", response.getBody().getUsername());
    }

    @Test
    void testGetUserNotFound() {
        when(userController.getUser(1L)).thenReturn(null);
        
        ResponseEntity<User> response = userRestService.getUser(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testLoginSuccess() {
        User loginUser = new User();
        loginUser.setEmail("test@example.com");
        loginUser.setPassword("password123");
        
        User authenticatedUser = new User();
        authenticatedUser.setId(1L);
        authenticatedUser.setUsername("testuser");
        
        when(userController.login(any(User.class))).thenReturn(authenticatedUser);
        
        ResponseEntity<?> response = userRestService.login(loginUser);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authenticatedUser, response.getBody());
    }

    @Test
    void testLoginFailedInvalidCredentials() {
        User loginUser = new User();
        loginUser.setEmail("test@example.com");
        loginUser.setPassword("wrongpassword");
        
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setLocked(false);
        existingUser.setVerified(true);
        
        when(userController.login(any(User.class))).thenReturn(null);
        when(userController.findByEmail("test@example.com")).thenReturn(existingUser);
        
        ResponseEntity<?> response = userRestService.login(loginUser);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid username or password", response.getBody());
    }

    @Test
    void testLoginFailedAccountLocked() {
        User loginUser = new User();
        loginUser.setEmail("test@example.com");
        loginUser.setPassword("password123");
        
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setLocked(true);
        
        when(userController.login(any(User.class))).thenReturn(null);
        when(userController.findByEmail("test@example.com")).thenReturn(existingUser);
        
        ResponseEntity<?> response = userRestService.login(loginUser);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Account is locked. Please contact support.", response.getBody());
    }

    @Test
    void testLoginFailedEmailNotVerified() {
        User loginUser = new User();
        loginUser.setEmail("test@example.com");
        loginUser.setPassword("password123");
        
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setLocked(false);
        existingUser.setVerified(false);
        
        when(userController.login(any(User.class))).thenReturn(null);
        when(userController.findByEmail("test@example.com")).thenReturn(existingUser);
        
        ResponseEntity<?> response = userRestService.login(loginUser);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Email not verified. Please check your email for verification link.", responseBody.get("message"));
        assertEquals(1L, responseBody.get("userId"));
        assertEquals(true, responseBody.get("unverified"));
    }

    @Test
    void testCreateUserSuccess() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("password123");
        
        User createdUser = new User();
        createdUser.setId(1L);
        createdUser.setUsername("newuser");
        
        when(userController.createUser(any(User.class))).thenReturn(createdUser);
        
        ResponseEntity<Map<String, Object>> response = userRestService.create(newUser);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User created. Please check your email to verify your account.", response.getBody().get("message"));
        assertEquals(1L, response.getBody().get("userId"));
    }

    @Test
    void testCreateUserUsernameExists() {
        User newUser = new User();
        newUser.setUsername("existinguser");
        newUser.setEmail("new@example.com");
        
        when(userController.createUser(any(User.class))).thenReturn(null);
        when(userController.userExistsByUsername("existinguser")).thenReturn(true);
        
        ResponseEntity<Map<String, Object>> response = userRestService.create(newUser);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username already in use.", response.getBody().get("message"));
    }

    @Test
    void testCreateUserEmailExists() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("existing@example.com");
        
        when(userController.createUser(any(User.class))).thenReturn(null);
        when(userController.userExistsByUsername("newuser")).thenReturn(false);
        when(userController.userExistsByEmail("existing@example.com")).thenReturn(true);
        
        ResponseEntity<Map<String, Object>> response = userRestService.create(newUser);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email already in use.", response.getBody().get("message"));
    }

    @Test
    void testGoogleAuthSuccess() {
        GoogleAuthRequest request = new GoogleAuthRequest();
        request.setToken("valid-token");
        
        User authenticatedUser = new User();
        authenticatedUser.setId(1L);
        authenticatedUser.setUsername("googleuser");
        
        when(userController.loginWithGoogle(any(GoogleAuthRequest.class))).thenReturn(authenticatedUser);
        
        ResponseEntity<?> response = userRestService.googleAuth(request);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authenticatedUser, response.getBody());
    }

    @Test
    void testGoogleAuthFailed() {
        GoogleAuthRequest request = new GoogleAuthRequest();
        request.setToken("invalid-token");
        
        when(userController.loginWithGoogle(any(GoogleAuthRequest.class))).thenReturn(null);
        
        ResponseEntity<?> response = userRestService.googleAuth(request);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("Failed to authenticate with Google", responseBody.get("message"));
    }

    @Test
    void testUpdateUsernameSuccess() {
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("newusername");
        
        when(userController.updateUsername(1L, "newusername")).thenReturn(updatedUser);
        
        ResponseEntity<Map<String, String>> response = userRestService.updateUsername(1L, "newusername");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Username updated.", response.getBody().get("message"));
    }

    @Test
    void testUpdateUsernameUserNotFound() {
        when(userController.updateUsername(1L, "newusername")).thenReturn(null);
        when(userController.getUser(1L)).thenReturn(null);
        
        ResponseEntity<Map<String, String>> response = userRestService.updateUsername(1L, "newusername");
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found.", response.getBody().get("message"));
    }

    @Test
    void testUpdateUsernameAlreadyExists() {
        User existingUser = new User();
        existingUser.setId(1L);
        
        when(userController.updateUsername(1L, "existingusername")).thenReturn(null);
        when(userController.getUser(1L)).thenReturn(existingUser);
        
        ResponseEntity<Map<String, String>> response = userRestService.updateUsername(1L, "existingusername");
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username already in use.", response.getBody().get("message"));
    }

    @Test
    void testVerifyEmailSuccess() {
        when(userController.verifyEmail("valid-token")).thenReturn(true);
        
        ResponseEntity<Map<String, String>> response = userRestService.verifyEmail("valid-token");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Email verified successfully. You can now login.", response.getBody().get("message"));
    }

    @Test
    void testVerifyEmailInvalidToken() {
        when(userController.verifyEmail("invalid-token")).thenReturn(false);
        
        ResponseEntity<Map<String, String>> response = userRestService.verifyEmail("invalid-token");
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid or expired verification token.", response.getBody().get("message"));
    }

    @Test
    void testResendVerificationEmailSuccess() {
        when(userController.resendVerificationEmail(1L)).thenReturn(true);
        
        ResponseEntity<Map<String, String>> response = userRestService.resendVerificationEmail(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Verification email has been resent.", response.getBody().get("message"));
    }

    @Test
    void testResendVerificationEmailUserNotFound() {
        when(userController.resendVerificationEmail(1L)).thenReturn(false);
        when(userController.getUser(1L)).thenReturn(null);
        
        ResponseEntity<Map<String, String>> response = userRestService.resendVerificationEmail(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found.", response.getBody().get("message"));
    }

    @Test
    void testExportUserDataSuccess() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("user", new HashMap<String, Object>() {{
            put("id", 1L);
            put("username", "testuser");
        }});
        
        when(userController.getUserProfileData(1L)).thenReturn(userData);
        doNothing().when(userController).logDataAccess(anyLong(), anyString(), anyString());
        
        ResponseEntity<Map<String, Object>> response = userRestService.exportUserData(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userData, response.getBody());
        verify(userController).logDataAccess(eq(1L), eq("data_export"), anyString());
    }

    @Test
    void testExportUserDataUserNotFound() {
        when(userController.getUserProfileData(1L)).thenReturn(null);
        
        ResponseEntity<Map<String, Object>> response = userRestService.exportUserData(1L);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}