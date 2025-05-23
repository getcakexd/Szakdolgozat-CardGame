package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.PasswordResetController;
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
class PasswordResetRestServiceTest {

    @Mock
    private PasswordResetController passwordResetController;

    @InjectMocks
    private PasswordResetRestService passwordResetRestService;

    @Test
    void testForgotPasswordSuccess() {
        when(passwordResetController.createPasswordResetTokenForUser(anyString())).thenReturn(true);
        
        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        
        ResponseEntity<Map<String, String>> response = passwordResetRestService.forgotPassword(request);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("If an account exists with that email, a password reset link has been sent.", response.getBody().get("message"));
    }

    @Test
    void testForgotPasswordEmptyEmail() {
        Map<String, String> request = new HashMap<>();
        
        ResponseEntity<Map<String, String>> response = passwordResetRestService.forgotPassword(request);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email is required.", response.getBody().get("message"));
    }

    @Test
    void testValidateResetTokenValid() {
        when(passwordResetController.validatePasswordResetToken("test-token")).thenReturn(true);
        
        ResponseEntity<Map<String, Boolean>> response = passwordResetRestService.validateResetToken("test-token");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().get("valid"));
    }

    @Test
    void testValidateResetTokenInvalid() {
        when(passwordResetController.validatePasswordResetToken("test-token")).thenReturn(false);
        
        ResponseEntity<Map<String, Boolean>> response = passwordResetRestService.validateResetToken("test-token");
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().get("valid"));
    }

    @Test
    void testResetPasswordSuccess() {
        when(passwordResetController.resetPassword("test-token", "new-password")).thenReturn(true);
        
        Map<String, String> request = new HashMap<>();
        request.put("token", "test-token");
        request.put("newPassword", "new-password");
        
        ResponseEntity<Map<String, String>> response = passwordResetRestService.resetPassword(request);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password has been reset successfully.", response.getBody().get("message"));
    }

    @Test
    void testResetPasswordMissingData() {
        Map<String, String> request = new HashMap<>();
        
        ResponseEntity<Map<String, String>> response = passwordResetRestService.resetPassword(request);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Token and new password are required.", response.getBody().get("message"));
    }

    @Test
    void testResetPasswordFailure() {
        when(passwordResetController.resetPassword("test-token", "new-password")).thenReturn(false);
        
        Map<String, String> request = new HashMap<>();
        request.put("token", "test-token");
        request.put("newPassword", "new-password");
        
        ResponseEntity<Map<String, String>> response = passwordResetRestService.resetPassword(request);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Failed to reset password. The link may be invalid or expired.", response.getBody().get("message"));
    }
}