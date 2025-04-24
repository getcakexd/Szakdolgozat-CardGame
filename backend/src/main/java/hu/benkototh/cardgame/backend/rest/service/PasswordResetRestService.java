package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.PasswordResetController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class PasswordResetRestService {

    @Autowired
    private PasswordResetController passwordResetController;

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            response.put("message", "Email is required.");
            return ResponseEntity.badRequest().body(response);
        }

        boolean emailSent = passwordResetController.createPasswordResetTokenForUser(email);

        response.put("message", "If an account exists with that email, a password reset link has been sent.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<Map<String, Boolean>> validateResetToken(@RequestParam String token) {
        Map<String, Boolean> response = new HashMap<>();
        boolean isValid = passwordResetController.validatePasswordResetToken(token);

        response.put("valid", isValid);

        if (isValid) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
        Map<String, String> response = new HashMap<>();
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || token.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            response.put("message", "Token and new password are required.");
            return ResponseEntity.badRequest().body(response);
        }

        boolean resetSuccessful = passwordResetController.resetPassword(token, newPassword);

        if (!resetSuccessful) {
            response.put("message", "Failed to reset password. The link may be invalid or expired.");
            return ResponseEntity.badRequest().body(response);
        }

        response.put("message", "Password has been reset successfully.");
        return ResponseEntity.ok(response);
    }
}
