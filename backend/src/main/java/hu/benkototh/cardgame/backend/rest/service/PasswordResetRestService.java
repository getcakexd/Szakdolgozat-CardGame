package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.PasswordResetController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Password Reset", description = "Operations for password reset functionality")
public class PasswordResetRestService {

    @Autowired
    private PasswordResetController passwordResetController;

    @Operation(summary = "Request password reset", description = "Initiates the password reset process by sending an email with a reset link")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset email sent if account exists")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Parameter(description = "Request containing email", required = true) @RequestBody Map<String, String> request) {
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

    @Operation(summary = "Validate reset token", description = "Validates if a password reset token is valid and not expired")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token is valid"),
            @ApiResponse(responseCode = "400", description = "Token is invalid or expired")
    })
    @GetMapping("/validate-reset-token")
    public ResponseEntity<Map<String, Boolean>> validateResetToken(
            @Parameter(description = "Reset token", required = true) @RequestParam String token) {
        Map<String, Boolean> response = new HashMap<>();
        boolean isValid = passwordResetController.validatePasswordResetToken(token);

        response.put("valid", isValid);

        if (isValid) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Reset password", description = "Resets a user's password using a valid token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or token")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Parameter(description = "Request containing token and new password", required = true) @RequestBody Map<String, String> request) {
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