package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.Data.GoogleAuthRequest;
import hu.benkototh.cardgame.backend.rest.controller.UserController;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.Data.UserHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserRestService {

    @Autowired
    private UserController userController;

    @GetMapping("all")
    public ResponseEntity<List<User>> all() {
        return ResponseEntity.ok(userController.getAllUsers());
    }

    @GetMapping("/get")
    public ResponseEntity<User> getUser(@RequestParam long userId) {
        User user = userController.getUser(userId);

        if (user == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
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

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> create(@RequestBody User user) {
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

    @PostMapping("/google-auth")
    public ResponseEntity<?> googleAuth(@RequestBody GoogleAuthRequest googleAuthRequest) {
        User user = userController.loginWithGoogle(googleAuthRequest);

        if (user == null) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to authenticate with Google");
            return ResponseEntity.status(401).body(response);
        }

        return ResponseEntity.ok(user);
    }

    @PutMapping("/update/username")
    public ResponseEntity<Map<String, String>> updateUsername(@RequestParam long userId, @RequestParam String newUsername) {
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

    @PutMapping("/update/email")
    public ResponseEntity<Map<String, String>> updateEmail(@RequestParam long userId, @RequestParam String newEmail) {
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

    @PutMapping("/update/password")
    public ResponseEntity<Map<String, String>> updatePassword(@RequestParam long userId, @RequestParam String currentPassword, @RequestParam String newPassword) {
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

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteUser(@RequestParam long userId, @RequestParam String password) {
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

    @GetMapping("/has-google-auth-password")
    public ResponseEntity<Map<String, Boolean>> hasGoogleAuthPassword(@RequestParam long userId) {
        Map<String, Boolean> response = new HashMap<>();
        boolean hasGoogleAuthPassword = userController.hasGoogleAuthPassword(userId);
        response.put("hasGoogleAuthPassword", hasGoogleAuthPassword);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/set-password")
    public ResponseEntity<Map<String, String>> setPassword(@RequestParam long userId, @RequestParam String newPassword) {
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

    @GetMapping("/history")
    public ResponseEntity<List<UserHistory>> getUserHistory(@RequestParam long userId) {
        List<UserHistory> history = userController.getUserHistory(userId);

        if (history == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(history);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
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

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(@RequestParam long userId) {
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

    @GetMapping("/export-data/{userId}")
    public ResponseEntity<Map<String, Object>> exportUserData(@PathVariable long userId) {
        Map<String, Object> userData = userController.getUserProfileData(userId);

        if (userData == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        userController.logDataAccess(userId, "data_export", "User exported their data: " + userId);

        return ResponseEntity.ok(userData);
    }
}
