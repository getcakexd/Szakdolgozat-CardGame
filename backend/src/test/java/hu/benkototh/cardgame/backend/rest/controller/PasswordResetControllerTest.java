package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.PasswordResetToken;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IPasswordResetTokenRepository;
import hu.benkototh.cardgame.backend.rest.service.SimpleEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetControllerTest {

    @Mock
    private UserController userController;

    @Mock
    private IPasswordResetTokenRepository tokenRepository;

    @Mock
    private SimpleEmailService emailService;

    @Mock
    private AuditLogController auditLogController;

    @InjectMocks
    private PasswordResetController passwordResetController;

    private User testUser;
    private PasswordResetToken testToken;
    private List<PasswordResetToken> tokens;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");

        testToken = new PasswordResetToken();
        testToken.setId(1L);
        testToken.setToken("test-token");
        testToken.setUser(testUser);
        testToken.setExpiryDate(LocalDateTime.now().plusHours(24));

        tokens = new ArrayList<>();
        tokens.add(testToken);

        ReflectionTestUtils.setField(passwordResetController, "tokenExpiryHours", 24);

        BCryptPasswordEncoder passwordEncoder = mock(BCryptPasswordEncoder.class);
        ReflectionTestUtils.setField(passwordResetController, "passwordEncoder", passwordEncoder);
    }

    @Test
    void testCreatePasswordResetTokenForUserSuccess() {
        when(userController.findByEmail("test@example.com")).thenReturn(testUser);
        when(tokenRepository.findAll()).thenReturn(new ArrayList<>());
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString(), anyLong());
        
        boolean result = passwordResetController.createPasswordResetTokenForUser("test@example.com");
        
        assertTrue(result);
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq("test@example.com"), anyString(), eq(1L));
        verify(auditLogController).logAction(eq("PASSWORD_RESET_EMAIL_SENT"), eq(1L), anyString());
    }

    @Test
    void testCreatePasswordResetTokenForUserUserNotFound() {
        when(userController.findByEmail("test@example.com")).thenReturn(null);
        
        boolean result = passwordResetController.createPasswordResetTokenForUser("test@example.com");
        
        assertFalse(result);
        verify(auditLogController).logAction(eq("PASSWORD_RESET_REQUEST_FAILED"), eq(0L), anyString());
    }

    @Test
    void testCreatePasswordResetTokenForUserExistingToken() {
        when(userController.findByEmail("test@example.com")).thenReturn(testUser);
        when(tokenRepository.findAll()).thenReturn(tokens);
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString(), anyLong());
        
        boolean result = passwordResetController.createPasswordResetTokenForUser("test@example.com");
        
        assertTrue(result);
        verify(tokenRepository).delete(testToken);
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq("test@example.com"), anyString(), eq(1L));
    }

    @Test
    void testCreatePasswordResetTokenForUserEmailException() {
        when(userController.findByEmail("test@example.com")).thenReturn(testUser);
        when(tokenRepository.findAll()).thenReturn(new ArrayList<>());
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);
        doThrow(new RuntimeException("Email error")).when(emailService).sendPasswordResetEmail(anyString(), anyString(), anyLong());
        
        boolean result = passwordResetController.createPasswordResetTokenForUser("test@example.com");
        
        assertFalse(result);
        verify(auditLogController).logAction(eq("PASSWORD_RESET_EMAIL_FAILED"), eq(1L), anyString());
    }

    @Test
    void testValidatePasswordResetTokenValid() {
        when(tokenRepository.findAll()).thenReturn(tokens);
        
        boolean result = passwordResetController.validatePasswordResetToken("test-token");
        
        assertTrue(result);
        verify(auditLogController).logAction(eq("PASSWORD_RESET_TOKEN_VALIDATED"), eq(1L), anyString());
    }

    @Test
    void testValidatePasswordResetTokenNotFound() {
        when(tokenRepository.findAll()).thenReturn(new ArrayList<>());
        
        boolean result = passwordResetController.validatePasswordResetToken("test-token");
        
        assertFalse(result);
        verify(auditLogController).logAction(eq("PASSWORD_RESET_TOKEN_INVALID"), eq(0L), anyString());
    }

    @Test
    void testValidatePasswordResetTokenExpired() {
        testToken.setExpiryDate(LocalDateTime.now().minusHours(24));
        when(tokenRepository.findAll()).thenReturn(tokens);
        
        boolean result = passwordResetController.validatePasswordResetToken("test-token");
        
        assertFalse(result);
        verify(auditLogController).logAction(eq("PASSWORD_RESET_TOKEN_INVALID"), eq(0L), anyString());
    }

    @Test
    void testResetPasswordTokenNotFound() {
        when(tokenRepository.findAll()).thenReturn(new ArrayList<>());
        
        boolean result = passwordResetController.resetPassword("test-token", "new-password");
        
        assertFalse(result);
        verify(auditLogController).logAction(eq("PASSWORD_RESET_FAILED"), eq(0L), anyString());
    }

    @Test
    void testResetPasswordTokenExpired() {
        testToken.setExpiryDate(LocalDateTime.now().minusHours(24));
        when(tokenRepository.findAll()).thenReturn(tokens);
        
        boolean result = passwordResetController.resetPassword("test-token", "new-password");
        
        assertFalse(result);
        verify(auditLogController).logAction(eq("PASSWORD_RESET_FAILED"), eq(0L), anyString());
    }
}