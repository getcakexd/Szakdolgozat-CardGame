package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.game.controller.StatsController;
import hu.benkototh.cardgame.backend.game.model.GameStatistics;
import hu.benkototh.cardgame.backend.game.model.UserGameStats;
import hu.benkototh.cardgame.backend.game.model.UserStats;
import hu.benkototh.cardgame.backend.rest.Data.*;
import hu.benkototh.cardgame.backend.rest.repository.IUserHistoryRepository;
import hu.benkototh.cardgame.backend.rest.repository.IUserRepository;
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

    @Mock
    private StatsController statsController;

    @Mock
    private FriendshipController friendshipController;

    @Mock
    private ChatController chatController;

    @Mock
    private ClubMemberController clubMemberController;

    @Mock
    private LobbyController lobbyController;

    @Mock
    private TicketController ticketController;

    @Mock
    private ClubChatController clubChatController;

    @Spy
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private UserController userController;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userController, "GOOGLE_AUTH_PREFIX", "google_");
        ReflectionTestUtils.setField(userController, "GOOGLE_AUTH_SUFFIX", "auth");
        ReflectionTestUtils.setField(userController, "verificationTokenExpirationHours", 24);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole("ROLE_USER");
        testUser.setVerified(true);
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
    void testGetUser_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        User result = userController.getUser(999L);

        assertNull(result);
        verify(userRepository).findById(999L);
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
    void testLoginFailedUserNotFound() {
        User loginUser = new User();
        loginUser.setEmail("nonexistent@example.com");
        loginUser.setPassword("password123");

        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        User result = userController.login(loginUser);

        assertNull(result);
        verify(auditLogController).logAction(eq("LOGIN_FAILED"), anyLong(), anyString());
    }

    @Test
    void testLoginFailedMaxAttemptsReached() {
        User loginUser = new User();
        loginUser.setEmail("test@example.com");
        loginUser.setPassword("wrongpassword");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("test@example.com");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setVerified(true);
        existingUser.setFailedLoginAttempts(4);

        when(userRepository.findAll()).thenReturn(Collections.singletonList(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userController.login(loginUser);

        assertNull(result);
        assertEquals(5, existingUser.getFailedLoginAttempts());
        assertTrue(existingUser.isLocked());
        verify(auditLogController).logAction(eq("ACCOUNT_LOCKED"), eq(1L), anyString());
        verify(userRepository).save(existingUser);
    }

    @Test
    void testCreateUserSuccess() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("password123");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("newuser");
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
    void testCreateUserAsFirstUser() {
        User newUser = new User();
        newUser.setUsername("rootuser");
        newUser.setEmail("root@example.com");
        newUser.setPassword("password123");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("rootuser");
        savedUser.setEmail("root@example.com");
        savedUser.setPassword(passwordEncoder.encode("password123"));
        savedUser.setRole("ROLE_ROOT");
        savedUser.setVerified(false);
        savedUser.setVerificationToken("token123");

        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString(), anyLong());

        User result = userController.createUser(newUser);

        assertNotNull(result);
        assertEquals("ROLE_ROOT", result.getRole());
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
    void testCreateUserEmailExists() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("existing@example.com");

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
    void testCreateUserEmailSendingFails() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("password123");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("newuser");
        savedUser.setEmail("new@example.com");
        savedUser.setPassword(passwordEncoder.encode("password123"));
        savedUser.setRole("ROLE_USER");
        savedUser.setVerified(false);
        savedUser.setVerificationToken("token123");

        when(userRepository.findAll()).thenReturn(new ArrayList<>());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        doThrow(new RuntimeException("Email sending failed")).when(emailService).sendVerificationEmail(anyString(), anyString(), anyLong());

        User result = userController.createUser(newUser);

        assertNotNull(result);
        verify(auditLogController).logAction(eq("VERIFICATION_EMAIL_FAILED"), eq(1L), anyString());
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
    void testResendVerificationEmailSuccess() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setVerified(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString(), anyLong());

        boolean result = userController.resendVerificationEmail(1L);

        assertTrue(result);
        assertNotNull(user.getVerificationToken());
        assertNotNull(user.getVerificationTokenExpiry());
        verify(userRepository).save(user);
        verify(emailService).sendVerificationEmail(eq("test@example.com"), anyString(), eq(1L));
        verify(auditLogController).logAction(eq("VERIFICATION_EMAIL_RESENT"), eq(1L), anyString());
    }

    @Test
    void testResendVerificationEmailUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = userController.resendVerificationEmail(999L);

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString(), anyLong());
    }

    @Test
    void testResendVerificationEmailAlreadyVerified() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setVerified(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = userController.resendVerificationEmail(1L);

        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString(), anyLong());
    }

    @Test
    void testResendVerificationEmailSendingFails() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setVerified(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        doThrow(new RuntimeException("Email sending failed")).when(emailService).sendVerificationEmail(anyString(), anyString(), anyLong());

        boolean result = userController.resendVerificationEmail(1L);

        assertFalse(result);
        verify(userRepository).save(user);
        verify(auditLogController).logAction(eq("VERIFICATION_EMAIL_FAILED"), eq(1L), anyString());
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
    void testLoginWithGoogleInvalidToken() {
        GoogleAuthRequest request = new GoogleAuthRequest();
        request.setToken("invalid-token");

        when(googleTokenVerifier.verify("invalid-token")).thenReturn(null);

        User result = userController.loginWithGoogle(request);

        assertNull(result);
        verify(auditLogController).logAction(eq("GOOGLE_LOGIN_FAILED"), eq(0L), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginWithGoogleException() {
        GoogleAuthRequest request = new GoogleAuthRequest();
        request.setToken("valid-token");

        when(googleTokenVerifier.verify("valid-token")).thenThrow(new RuntimeException("Verification failed"));

        User result = userController.loginWithGoogle(request);

        assertNull(result);
        verify(auditLogController).logAction(eq("GOOGLE_LOGIN_FAILED"), eq(0L), anyString());
        verify(userRepository, never()).save(any(User.class));
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

    @Test
    void testUpdateUsernameUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        User result = userController.updateUsername(999L, "newusername");

        assertNull(result);
        verify(auditLogController).logAction(eq("USERNAME_UPDATE_FAILED"), eq(999L), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateUsernameAlreadyExists() {
        User user = new User();
        user.setId(1L);
        user.setUsername("oldusername");

        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setUsername("newusername");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findAll()).thenReturn(Arrays.asList(user, existingUser));

        User result = userController.updateUsername(1L, "newusername");

        assertNull(result);
        verify(auditLogController).logAction(eq("USERNAME_UPDATE_FAILED"), eq(1L), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateEmailSuccess() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("old@example.com");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("new@example.com");
        updatedUser.setVerified(false);
        updatedUser.setVerificationToken("new-token");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString(), anyLong());

        User result = userController.updateEmail(1L, "new@example.com");

        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail());
        assertFalse(result.isVerified());
        assertNotNull(result.getVerificationToken());
        verify(userRepository).save(any(User.class));
        verify(userHistoryRepository).save(any());
        verify(emailService).sendVerificationEmail(eq("new@example.com"), anyString(), eq(1L));
        verify(auditLogController).logAction(eq("EMAIL_UPDATED"), eq(1L), anyString());
    }

    @Test
    void testUpdateEmailUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        User result = userController.updateEmail(999L, "new@example.com");

        assertNull(result);
        verify(auditLogController).logAction(eq("EMAIL_UPDATE_FAILED"), eq(999L), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateEmailAlreadyExists() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("old@example.com");

        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setUsername("otheruser");
        existingUser.setEmail("new@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findAll()).thenReturn(Arrays.asList(user, existingUser));

        User result = userController.updateEmail(1L, "new@example.com");

        assertNull(result);
        verify(auditLogController).logAction(eq("EMAIL_UPDATE_FAILED"), eq(1L), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdateEmailSendingFails() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("old@example.com");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("new@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        doThrow(new RuntimeException("Email sending failed")).when(emailService).sendVerificationEmail(anyString(), anyString(), anyLong());

        User result = userController.updateEmail(1L, "new@example.com");

        assertNotNull(result);
        verify(auditLogController).logAction(eq("VERIFICATION_EMAIL_FAILED"), eq(1L), anyString());
    }

    @Test
    void testUpdatePasswordSuccess() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("oldpassword"));

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setPassword(passwordEncoder.encode("newpassword"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userController.updatePassword(1L, "oldpassword", "newpassword");

        assertNotNull(result);
        assertTrue(passwordEncoder.matches("newpassword", result.getPassword()));
        verify(userRepository).save(any(User.class));
        verify(auditLogController).logAction(eq("PASSWORD_UPDATED"), eq(1L), anyString());
    }

    @Test
    void testUpdatePasswordUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        User result = userController.updatePassword(999L, "oldpassword", "newpassword");

        assertNull(result);
        verify(auditLogController).logAction(eq("PASSWORD_UPDATE_FAILED"), eq(999L), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdatePasswordIncorrectCurrentPassword() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("correctpassword"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userController.updatePassword(1L, "wrongpassword", "newpassword");

        assertNull(result);
        verify(auditLogController).logAction(eq("PASSWORD_UPDATE_FAILED"), eq(1L), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testUpdatePasswordSameAsOld() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("password123"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userController.updatePassword(1L, "password123", "password123");

        assertNull(result);
        verify(auditLogController).logAction(eq("PASSWORD_UPDATE_FAILED"), eq(1L), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeleteUserSuccess() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("password123"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        boolean result = userController.deleteUser(1L, "password123");

        assertTrue(result);
        verify(userRepository).delete(user);
        verify(auditLogController).logAction(eq("USER_DELETED"), eq(1L), anyString());
    }

    @Test
    void testDeleteUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = userController.deleteUser(999L, "password123");

        assertFalse(result);
        verify(auditLogController).logAction(eq("USER_DELETION_FAILED"), eq(999L), anyString());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void testDeleteUserIncorrectPassword() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("correctpassword"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = userController.deleteUser(1L, "wrongpassword");

        assertFalse(result);
        verify(auditLogController).logAction(eq("USER_DELETION_FAILED"), eq(1L), anyString());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void testHasGoogleAuthPasswordTrue() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("google_auth"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = userController.hasGoogleAuthPassword(1L);

        assertTrue(result);
    }

    @Test
    void testHasGoogleAuthPasswordFalse() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("regularpassword"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = userController.hasGoogleAuthPassword(1L);

        assertFalse(result);
    }

    @Test
    void testHasGoogleAuthPasswordUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = userController.hasGoogleAuthPassword(999L);

        assertFalse(result);
    }

    @Test
    void testSetPasswordSuccess() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("oldpassword"));

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setPassword(passwordEncoder.encode("newpassword"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userController.setPassword(1L, "newpassword");

        assertNotNull(result);
        assertTrue(passwordEncoder.matches("newpassword", result.getPassword()));
        verify(userRepository).save(any(User.class));
        verify(auditLogController).logAction(eq("PASSWORD_SET"), eq(1L), anyString());
    }

    @Test
    void testSetPasswordUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        User result = userController.setPassword(999L, "newpassword");

        assertNull(result);
        verify(auditLogController).logAction(eq("PASSWORD_SET_FAILED"), eq(999L), anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetUserHistorySuccess() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        UserHistory history1 = new UserHistory();
        history1.setUser(user);
        history1.setPreviousUsername("oldusername");

        UserHistory history2 = new UserHistory();
        history2.setUser(user);
        history2.setPreviousEmail("old@example.com");

        List<UserHistory> histories = Arrays.asList(history1, history2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userHistoryRepository.findAll()).thenReturn(histories);

        List<UserHistory> result = userController.getUserHistory(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetUserHistoryUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        List<UserHistory> result = userController.getUserHistory(999L);

        assertNull(result);
    }

    @Test
    void testAuthenticateUserSuccess() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password123");

        User storedUser = new User();
        storedUser.setId(1L);
        storedUser.setUsername("testuser");
        storedUser.setPassword(passwordEncoder.encode("password123"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(storedUser));

        boolean result = userController.authenticateUser(user);

        assertTrue(result);
        verify(auditLogController).logAction(eq("USER_AUTHENTICATED"), eq(1L), anyString());
    }

    @Test
    void testAuthenticateUserFailed() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("wrongpassword");

        User storedUser = new User();
        storedUser.setId(1L);
        storedUser.setUsername("testuser");
        storedUser.setPassword(passwordEncoder.encode("password123"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(storedUser));

        boolean result = userController.authenticateUser(user);

        assertFalse(result);
        verify(auditLogController).logAction(eq("USER_AUTHENTICATION_FAILED"), eq(1L), anyString());
    }

    @Test
    void testAuthenticateUserNotFound() {
        User user = new User();
        user.setId(999L);
        user.setUsername("nonexistent");
        user.setPassword("password123");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = userController.authenticateUser(user);

        assertFalse(result);
        verify(auditLogController).logAction(eq("USER_AUTHENTICATION_FAILED"), eq(999L), anyString());
    }

    @Test
    void testLogDataAccess() {
        userController.logDataAccess(1L, "profile", "view");

        verify(auditLogController).logAction(eq("DATA_ACCESS"), eq(1L), contains("profile"));
    }

    @Test
    void testFindByUsername() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));

        User result = userController.findByUsername("testuser");

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testFindByUsernameNotFound() {
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        User result = userController.findByUsername("nonexistent");

        assertNull(result);
    }

    @Test
    void testFindByEmail() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));

        User result = userController.findByEmail("test@example.com");

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testFindByEmailNotFound() {
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        User result = userController.findByEmail("nonexistent@example.com");

        assertNull(result);
    }

    @Test
    void testGetUserProfileData() {
        User user = testUser;

        UserStats userStats = new UserStats();
        userStats.setGamesPlayed(10);
        userStats.setGamesWon(5);

        UserGameStats gameStats = new UserGameStats();
        gameStats.setGamesPlayed(5);
        gameStats.setGamesWon(3);
        Game game = new Game();
        game.setId(1L);
        game.setName("Test Game");
        gameStats.setGameDefinition(game);

        GameStatistics recentGame = new GameStatistics();
        recentGame.setGameId("game-123");
        recentGame.setScore(100);

        User friend = new User();
        friend.setId(2L);
        friend.setUsername("friend");

        Message message = new Message();
        message.setId(1L);
        message.setContent("Hello");
        User receiver = new User();
        receiver.setId(3L);
        receiver.setUsername("receiver");
        message.setReceiver(receiver);

        Club club = new Club();
        club.setId(1L);
        club.setName("Test Club");

        ClubMessage clubMessage = new ClubMessage();
        clubMessage.setId(1L);
        clubMessage.setContent("Club message");
        clubMessage.setClub(club);

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setReference("TKT-123");
        ticket.setSubject("Test ticket");

        TicketMessage ticketMessage = new TicketMessage();
        ticketMessage.setId(1L);
        ticketMessage.setMessage("Ticket message");
        ticketMessage.setTicket(ticket);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(statsController.getUserStats(1L)).thenReturn(userStats);
        when(statsController.getUserGameStats(1L)).thenReturn(Collections.singletonList(gameStats));
        when(statsController.getRecentGames(eq(1L), anyInt())).thenReturn(Collections.singletonList(recentGame));
        when(friendshipController.getFriends(1L)).thenReturn(Collections.singletonList(friend));
        when(chatController.getSentMessagesByUser(1L)).thenReturn(Collections.singletonList(message));
        when(clubMemberController.getClubsByUser(user)).thenReturn(Collections.singletonList(club));
        when(clubMemberController.getClubMemberRole(1L, 1L)).thenReturn("MEMBER");
        when(clubChatController.getMessagesByUser(1L)).thenReturn(Collections.singletonList(clubMessage));
        when(ticketController.getUserTickets(1L)).thenReturn(Collections.singletonList(ticket));
        when(ticketController.getMessagesByUser(1L)).thenReturn(Collections.singletonList(ticketMessage));

        Map<String, Object> result = userController.getUserProfileData(1L);

        assertNotNull(result);
        assertTrue(result.containsKey("user"));
        assertTrue(result.containsKey("stats"));
        assertTrue(result.containsKey("gameStats"));
        assertTrue(result.containsKey("recentGames"));
        assertTrue(result.containsKey("friends"));
        assertTrue(result.containsKey("messages"));
        assertTrue(result.containsKey("clubs"));
        assertTrue(result.containsKey("clubMessages"));
        assertTrue(result.containsKey("tickets"));
        assertTrue(result.containsKey("ticketMessages"));

        verify(auditLogController).logAction(eq("PROFILE_DATA_ACCESSED"), eq(1L), anyString());
    }

    @Test
    void testGetUserProfileDataUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Map<String, Object> result = userController.getUserProfileData(999L);

        assertNull(result);
    }

    @Test
    void testGetUserProfileDataWithNullCollections() {
        User user = testUser;

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(statsController.getUserStats(1L)).thenReturn(null);
        when(statsController.getUserGameStats(1L)).thenReturn(null);
        when(statsController.getRecentGames(eq(1L), anyInt())).thenReturn(null);
        when(friendshipController.getFriends(1L)).thenReturn(null);
        when(chatController.getSentMessagesByUser(1L)).thenReturn(null);
        when(clubMemberController.getClubsByUser(user)).thenReturn(null);
        when(clubChatController.getMessagesByUser(1L)).thenReturn(null);
        when(ticketController.getUserTickets(1L)).thenReturn(null);
        when(ticketController.getMessagesByUser(1L)).thenReturn(null);

        Map<String, Object> result = userController.getUserProfileData(1L);

        assertNotNull(result);
        assertTrue(result.containsKey("user"));
        assertFalse(result.containsKey("stats"));
        assertFalse(result.containsKey("gameStats"));
        assertFalse(result.containsKey("recentGames"));
        assertFalse(result.containsKey("friends"));
        assertFalse(result.containsKey("messages"));
        assertFalse(result.containsKey("clubs"));
        assertFalse(result.containsKey("clubMessages"));
        assertFalse(result.containsKey("tickets"));
        assertFalse(result.containsKey("ticketMessages"));
    }

    @Test
    void testGetUserProfileDataWithEmptyCollections() {
        User user = testUser;

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(statsController.getUserStats(1L)).thenReturn(new UserStats());
        when(statsController.getUserGameStats(1L)).thenReturn(new ArrayList<>());
        when(statsController.getRecentGames(eq(1L), anyInt())).thenReturn(new ArrayList<>());
        when(friendshipController.getFriends(1L)).thenReturn(new ArrayList<>());
        when(chatController.getSentMessagesByUser(1L)).thenReturn(new ArrayList<>());
        when(clubMemberController.getClubsByUser(user)).thenReturn(new ArrayList<>());
        when(clubChatController.getMessagesByUser(1L)).thenReturn(new ArrayList<>());
        when(ticketController.getUserTickets(1L)).thenReturn(new ArrayList<>());
        when(ticketController.getMessagesByUser(1L)).thenReturn(new ArrayList<>());

        Map<String, Object> result = userController.getUserProfileData(1L);

        assertNotNull(result);
        assertTrue(result.containsKey("user"));
        assertTrue(result.containsKey("stats"));
        assertFalse(result.containsKey("gameStats"));
        assertFalse(result.containsKey("recentGames"));
        assertFalse(result.containsKey("friends"));
        assertFalse(result.containsKey("messages"));
        assertFalse(result.containsKey("clubs"));
        assertFalse(result.containsKey("clubMessages"));
        assertFalse(result.containsKey("tickets"));
        assertFalse(result.containsKey("ticketMessages"));
    }
}