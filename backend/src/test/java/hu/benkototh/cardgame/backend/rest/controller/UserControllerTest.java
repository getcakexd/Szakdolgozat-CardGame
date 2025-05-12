package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.GoogleAuthRequest;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserHistoryRepository;
import hu.benkototh.cardgame.backend.rest.service.SimpleEmailService;
import hu.benkototh.cardgame.backend.rest.util.GoogleTokenVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IUserHistoryRepository userHistoryRepository;

    @Mock
    private AuditLogController auditLogController;

    @Mock
    private GoogleTokenVerifier googleTokenVerifier;

    @Mock
    private SimpleEmailService emailService;

    @Spy
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userController, "GOOGLE_AUTH_PREFIX", "google_");
        ReflectionTestUtils.setField(userController, "GOOGLE_AUTH_SUFFIX", "auth");
        ReflectionTestUtils.setField(userController, "verificationTokenExpirationHours", 24);
    }

    @Test
    void testGetAllUsers() {
        List<User> users = new ArrayList<>();
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        users.add(user1);
        
        when(userRepository.findAll()).thenReturn(users);
        
        List<User> result = userController.getAllUsers();
        
        assertEquals(1, result.size());
        assertEquals("user1", result.get(0).getUsername());
        verify(userRepository).findAll();
    }

    @Test
    void testGetUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        User result = userController.getUser(1L);
        
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void testLoginSuccess() {
        User loginUser = new User();
        loginUser.setEmail("test@example.com");
        loginUser.setPassword("password123");
        
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("test@example.com");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setVerified(true);
        
        when(userRepository.findAll()).thenReturn(Collections.singletonList(existingUser));
        
        User result = userController.login(loginUser);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(0, result.getFailedLoginAttempts());
        verify(auditLogController).logAction(eq("LOGIN_SUCCESS"), eq(1L), anyString());
    }

    @Test
    void testLoginFailedIncorrectPassword() {
        User loginUser = new User();
        loginUser.setEmail("test@example.com");
        loginUser.setPassword("wrongpassword");
        
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("test@example.com");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setVerified(true);
        existingUser.setFailedLoginAttempts(0);
        
        when(userRepository.findAll()).thenReturn(Collections.singletonList(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        
        User result = userController.login(loginUser);
        
        assertNull(result);
        assertEquals(1, existingUser.getFailedLoginAttempts());
        verify(auditLogController).logAction(eq("LOGIN_FAILED"), eq(1L), anyString());
        verify(userRepository).save(existingUser);
    }

    @Test
    void testLoginFailedAccountLocked() {
        User loginUser = new User();
        loginUser.setEmail("test@example.com");
        loginUser.setPassword("password123");
        
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("test@example.com");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setVerified(true);
        existingUser.setLocked(true);
        
        when(userRepository.findAll()).thenReturn(Collections.singletonList(existingUser));
        
        User result = userController.login(loginUser);
        
        assertNull(result);
        verify(auditLogController).logAction(eq("LOGIN_FAILED"), eq(1L), anyString());
    }

    @Test
    void testLoginFailedEmailNotVerified() {
        User loginUser = new User();
        loginUser.setEmail("test@example.com");
        loginUser.setPassword("password123");
        
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("test@example.com");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setVerified(false);
        
        when(userRepository.findAll()).thenReturn(Collections.singletonList(existingUser));
        
        User result = userController.login(loginUser);
        
        assertNull(result);
        verify(auditLogController).logAction(eq("LOGIN_FAILED"), eq(1L), anyString());
    }

    @Test
    void testCreateUserSuccess() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("password123");
        
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("saveduser");
        savedUser.setEmail("new@example.com");
        savedUser.setPassword(passwordEncoder.encode("password123"));
        savedUser.setRole("ROLE_USER");
        savedUser.setVerified(false);
        savedUser.setVerificationToken("token123");
        
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString(), anyLong());
        
        User result = userController.createUser(newUser);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ROLE_USER", result.getRole());
        assertFalse(result.isVerified());
        assertNotNull(result.getVerificationToken());
        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(eq("new@example.com"), anyString(), eq(1L));
        verify(auditLogController).logAction(eq("USER_CREATED"), eq(1L), anyString());
    }

    @Test
    void testCreateUserUsernameExists() {
        User newUser = new User();
        newUser.setUsername("existinguser");
        newUser.setEmail("new@example.com");
        
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        existingUser.setEmail("existing@example.com");
        
        when(userRepository.findAll()).thenReturn(Collections.singletonList(existingUser));
        
        User result = userController.createUser(newUser);
        
        assertNull(result);
        verify(auditLogController).logAction(eq("USER_CREATION_FAILED"), eq(0L), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testVerifyEmailSuccess() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setVerified(false);
        user.setVerificationToken("valid-token");
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 1);
        user.setVerificationTokenExpiry(calendar.getTime());
        
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        boolean result = userController.verifyEmail("valid-token");
        
        assertTrue(result);
        assertTrue(user.isVerified());
        assertNull(user.getVerificationToken());
        assertNull(user.getVerificationTokenExpiry());
        verify(userRepository).save(user);
        verify(auditLogController).logAction(eq("EMAIL_VERIFIED"), eq(1L), anyString());
    }

    @Test
    void testVerifyEmailTokenNotFound() {
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        
        boolean result = userController.verifyEmail("invalid-token");
        
        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testVerifyEmailTokenExpired() {
        User user = new User();
        user.setId(1L);
        user.setVerified(false);
        user.setVerificationToken("expired-token");
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -1);
        user.setVerificationTokenExpiry(calendar.getTime());
        
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));
        
        boolean result = userController.verifyEmail("expired-token");
        
        assertFalse(result);
        assertFalse(user.isVerified());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginWithGoogleSuccess() {
        GoogleAuthRequest request = new GoogleAuthRequest();
        request.setToken("valid-token");
        request.setName("John Doe");
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", "john@example.com");
        
        when(googleTokenVerifier.verify("valid-token")).thenReturn(payload);
        
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("john@example.com");
        savedUser.setUsername("johndoe");
        savedUser.setVerified(true);
        
        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        User result = userController.loginWithGoogle(request);
        
        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
        assertEquals("johndoe", result.getUsername());
        assertTrue(result.isVerified());
        verify(userRepository).save(any(User.class));
        verify(auditLogController).logAction(eq("USER_CREATED_VIA_GOOGLE"), eq(1L), anyString());
    }

    @Test
    void testLoginWithGoogleExistingUser() {
        GoogleAuthRequest request = new GoogleAuthRequest();
        request.setToken("valid-token");
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", "existing@example.com");
        
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("existing@example.com");
        existingUser.setUsername("existinguser");
        
        when(googleTokenVerifier.verify("valid-token")).thenReturn(payload);
        when(userRepository.findAll()).thenReturn(Collections.singletonList(existingUser));
        
        User result = userController.loginWithGoogle(request);
        
        assertNotNull(result);
        assertEquals("existing@example.com", result.getEmail());
        assertEquals("existinguser", result.getUsername());
        verify(userRepository, never()).save(any(User.class));
        verify(auditLogController).logAction(eq("GOOGLE_LOGIN_SUCCESS"), eq(1L), anyString());
    }

    @Test
    void testUpdateUsernameSuccess() {
        User user = new User();
        user.setId(1L);
        user.setUsername("oldusername");
        
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("newusername");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        
        User result = userController.updateUsername(1L, "newusername");
        
        assertNotNull(result);
        assertEquals("newusername", result.getUsername());
        verify(userRepository).save(any(User.class));
        verify(userHistoryRepository).save(any());
        verify(auditLogController).logAction(eq("USERNAME_UPDATED"), eq(1L), anyString());
    }
}