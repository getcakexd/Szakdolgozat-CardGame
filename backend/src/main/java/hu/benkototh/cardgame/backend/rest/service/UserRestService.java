package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.GoogleAuthRequest;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.Data.UserHistory;
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
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Operations for user account management")
public class UserRestService {

    @Autowired
    private UserController userController;

    @Operation(summary = "Get all users", description = "Retrieves a list of all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)))
    })
    @GetMapping("all")
    public ResponseEntity<List<User>> all() {
        return ResponseEntity.ok(userController.getAllUsers());
    }

    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/get")
    public ResponseEntity<User> getUser(
            @Parameter(description = "User ID", required = true) @RequestParam long userId) {
        User user = userController.getUser(userId);

        if (user == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Login", description = "Authenticates a user with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed - invalid credentials, account locked, or email not verified")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Parameter(description = "Login credentials", required = true) @RequestBody User user) {
        User loggedInUser = userController.login(user);

        if (loggedInUser == null) {
            User existingUser = userController.findByEmail(user.getEmail());

            if (existingUser == null) {
                return ResponseEntity.status(401).body("Invalid email or password");
            }

            if (existingUser.isLocked()) {
                return ResponseEntity.status(401).body("Account is locked. Please contact support.");
            }

            if (!existingUser.isVerified()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Email not verified. Please check your email for verification link.");
                response.put("userId", existingUser.getId());
                response.put("unverified", true);
                return ResponseEntity.status(401).body(response);
            }

            return ResponseEntity.status(401).body("Invalid username or password");
        }

        return ResponseEntity.ok(loggedInUser);
    }

    @Operation(summary = "Create user", description = "Registers a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request - username or email already in use")
    })
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(
            @Parameter(description = "User details", required = true) @RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        User createdUser = userController.createUser(user);

        if (createdUser == null) {
            if (userController.userExistsByUsername(user.getUsername())) {
                response.put("message", "Username already in use.");
            } else if (userController.userExistsByEmail(user.getEmail())) {
                response.put("message", "Email already in use.");
            } else {
                response.put("message", "Failed to create user.");
            }
            return ResponseEntity.status(400).body(response);
        }

        response.put("message", "User created. Please check your email to verify your account.");
        response.put("userId", createdUser.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Google authentication", description = "Authenticates a user with Google credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Google authentication successful",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401", description = "Google authentication failed")
    })
    @PostMapping("/google-auth")
    public ResponseEntity<?> googleAuth(
            @Parameter(description = "Google authentication details", required = true) @RequestBody GoogleAuthRequest googleAuthRequest) {
        User user = userController.loginWithGoogle(googleAuthRequest);

        if (user == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to authenticate with Google");
            return ResponseEntity.status(401).body(response);
        }

        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update username", description = "Updates a user's username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Username updated successfully"),
            @ApiResponse(responseCode = "400", description = "Username already in use"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/update/username")
    public ResponseEntity<Map<String, String>> updateUsername(
            @Parameter(description = "User ID", required = true) @RequestParam long userId,
            @Parameter(description = "New username", required = true) @RequestParam String newUsername) {
        Map<String, String> response = new HashMap<>();
        User updatedUser = userController.updateUsername(userId, newUsername);

        if (updatedUser == null) {
            if (userController.getUser(userId) == null) {
                response.put("message", "User not found.");
                return ResponseEntity.status(404).body(response);
            } else {
                response.put("message", "Username already in use.");
                return ResponseEntity.status(400).body(response);
            }
        }

        response.put("message", "Username updated.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update email", description = "Updates a user's email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email updated successfully"),
            @ApiResponse(responseCode = "400", description = "Email already in use"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/update/email")
    public ResponseEntity<Map<String, String>> updateEmail(
            @Parameter(description = "User ID", required = true) @RequestParam long userId,
            @Parameter(description = "New email address", required = true) @RequestParam String newEmail) {
        Map<String, String> response = new HashMap<>();
        User updatedUser = userController.updateEmail(userId, newEmail);

        if (updatedUser == null) {
            if (userController.getUser(userId) == null) {
                response.put("message", "User not found.");
                return ResponseEntity.status(404).body(response);
            } else {
                response.put("message", "Email already in use.");
                return ResponseEntity.status(400).body(response);
            }
        }

        response.put("message", "Email updated. Please check your new email address for verification.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update password", description = "Updates a user's password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request - incorrect current password or new password same as old"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/update/password")
    public ResponseEntity<Map<String, String>> updatePassword(
            @Parameter(description = "User ID", required = true) @RequestParam long userId,
            @Parameter(description = "Current password", required = true) @RequestParam String currentPassword,
            @Parameter(description = "New password", required = true) @RequestParam String newPassword) {
        Map<String, String> response = new HashMap<>();
        User updatedUser = userController.updatePassword(userId, currentPassword, newPassword);

        if (updatedUser == null) {
            User user = userController.getUser(userId);

            if (user == null) {
                response.put("message", "User not found.");
                return ResponseEntity.status(404).body(response);
            } else if (userController.passwordEncoder.matches(newPassword, user.getPassword())) {
                response.put("message", "New password cannot be the same as the old one.");
                return ResponseEntity.status(400).body(response);
            } else {
                response.put("message", "Incorrect password.");
                return ResponseEntity.status(400).body(response);
            }
        }

        response.put("message", "Password updated.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete user", description = "Deletes a user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request - incorrect password"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteUser(
            @Parameter(description = "User ID", required = true) @RequestParam long userId,
            @Parameter(description = "Password for confirmation", required = true) @RequestParam String password) {
        Map<String, String> response = new HashMap<>();
        boolean deleted = userController.deleteUser(userId, password);

        if (!deleted) {
            User user = userController.getUser(userId);

            if (user == null) {
                response.put("message", "User not found.");
                return ResponseEntity.status(404).body(response);
            } else {
                response.put("message", "Incorrect password.");
                return ResponseEntity.status(400).body(response);
            }
        }

        response.put("message", "User deleted.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Check if user has Google auth password", description = "Checks if a user who authenticated with Google has set a password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully checked Google auth password status")
    })
    @GetMapping("/has-google-auth-password")
    public ResponseEntity<Map<String, Boolean>> hasGoogleAuthPassword(
            @Parameter(description = "User ID", required = true) @RequestParam long userId) {
        Map<String, Boolean> response = new HashMap<>();
        boolean hasGoogleAuthPassword = userController.hasGoogleAuthPassword(userId);
        response.put("hasGoogleAuthPassword", hasGoogleAuthPassword);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Set password", description = "Sets a password for a user who authenticated with Google")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password set successfully"),
            @ApiResponse(responseCode = "400", description = "Failed to set password"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/set-password")
    public ResponseEntity<Map<String, String>> setPassword(
            @Parameter(description = "User ID", required = true) @RequestParam long userId,
            @Parameter(description = "New password", required = true) @RequestParam String newPassword) {
        Map<String, String> response = new HashMap<>();
        User updatedUser = userController.setPassword(userId, newPassword);

        if (updatedUser == null) {
            User user = userController.getUser(userId);

            if (user == null) {
                response.put("message", "User not found.");
                return ResponseEntity.status(404).body(response);
            } else {
                response.put("message", "Failed to set password.");
                return ResponseEntity.status(400).body(response);
            }
        }

        response.put("message", "Password set successfully.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user history", description = "Retrieves the activity history for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user history",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserHistory.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/history")
    public ResponseEntity<List<UserHistory>> getUserHistory(
            @Parameter(description = "User ID", required = true) @RequestParam long userId) {
        List<UserHistory> history = userController.getUserHistory(userId);

        if (history == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Verify email", description = "Verifies a user's email address using a token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired verification token")
    })
    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(
            @Parameter(description = "Verification token", required = true) @RequestParam String token) {
        Map<String, String> response = new HashMap<>();
        boolean verified = userController.verifyEmail(token);

        if (verified) {
            response.put("message", "Email verified successfully. You can now login.");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Invalid or expired verification token.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(summary = "Resend verification email", description = "Resends the email verification link to a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification email resent successfully"),
            @ApiResponse(responseCode = "400", description = "Email already verified"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Failed to resend verification email")
    })
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(
            @Parameter(description = "User ID", required = true) @RequestParam long userId) {
        Map<String, String> response = new HashMap<>();
        boolean sent = userController.resendVerificationEmail(userId);

        if (sent) {
            response.put("message", "Verification email has been resent.");
            return ResponseEntity.ok(response);
        } else {
            User user = userController.getUser(userId);

            if (user == null) {
                response.put("message", "User not found.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else if (user.isVerified()) {
                response.put("message", "Email is already verified.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            } else {
                response.put("message", "Failed to resend verification email.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }
    }

    @Operation(summary = "Export user data", description = "Exports all data associated with a user (GDPR compliance)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully exported user data"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/export-data/{userId}")
    public ResponseEntity<Map<String, Object>> exportUserData(
            @Parameter(description = "User ID", required = true) @PathVariable long userId) {
        Map<String, Object> userData = userController.getUserProfileData(userId);

        if (userData == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        userController.logDataAccess(userId, "data_export", "User exported their data: " + userId);

        return ResponseEntity.ok(userData);
    }
}