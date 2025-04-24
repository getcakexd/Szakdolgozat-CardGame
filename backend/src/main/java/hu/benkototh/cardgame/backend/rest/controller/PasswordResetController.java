package hu.benkototh.cardgame.backend.rest.controller;

import hu.benkototh.cardgame.backend.rest.Data.PasswordResetToken;
import hu.benkototh.cardgame.backend.rest.Data.User;
import hu.benkototh.cardgame.backend.rest.repository.IPasswordResetTokenRepository;
import hu.benkototh.cardgame.backend.rest.service.SimpleEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
public class PasswordResetController {

    @Autowired
    private UserController userController;

    @Autowired
    private IPasswordResetTokenRepository tokenRepository;

    @Autowired
    private SimpleEmailService emailService;

    @Autowired
    private AuditLogController auditLogController;

    @Value("${password.reset.token.expiry.hours:24}")
    private int tokenExpiryHours;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean createPasswordResetTokenForUser(String email) {
        User user = userController.findByEmail(email);

        if (user == null) {
            auditLogController.logAction("PASSWORD_RESET_REQUEST_FAILED", 0L,
                    "Password reset request failed: Email not found - " + email);
            return false;
        }

        Optional<PasswordResetToken> existingToken = findByUser(user);
        existingToken.ifPresent(tokenRepository::delete);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(tokenExpiryHours);
        PasswordResetToken resetToken = new PasswordResetToken(token, user, expiryDate);
        tokenRepository.save(resetToken);

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), token, user.getId());
            auditLogController.logAction("PASSWORD_RESET_EMAIL_SENT", user.getId(),
                    "Password reset email sent to: " + user.getEmail());
            return true;
        } catch (Exception e) {
            auditLogController.logAction("PASSWORD_RESET_EMAIL_FAILED", user.getId(),
                    "Failed to send password reset email: " + e.getMessage());
            return false;
        }
    }

    public boolean validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> resetToken = findByToken(token);

        if (resetToken.isEmpty() || resetToken.get().isExpired()) {
            auditLogController.logAction("PASSWORD_RESET_TOKEN_INVALID", 0L,
                    "Invalid or expired password reset token: " + token);
            return false;
        }

        auditLogController.logAction("PASSWORD_RESET_TOKEN_VALIDATED", resetToken.get().getUser().getId(),
                "Password reset token validated for user: " + resetToken.get().getUser().getUsername());
        return true;
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> resetToken = findByToken(token);

        if (resetToken.isEmpty() || resetToken.get().isExpired()) {
            auditLogController.logAction("PASSWORD_RESET_FAILED", 0L,
                    "Password reset failed: Invalid or expired token");
            return false;
        }

        User user = resetToken.get().getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userController.userRepository.save(user);

        tokenRepository.delete(resetToken.get());

        auditLogController.logAction("PASSWORD_RESET_SUCCESS", user.getId(),
                "Password reset successful for user: " + user.getUsername());
        return true;
    }

    private Optional<PasswordResetToken> findByToken(String token) {
        return tokenRepository.findAll().stream()
                .filter(t -> t.getToken().equals(token))
                .findFirst();
    }

    private Optional<PasswordResetToken> findByUser(User user) {
        return tokenRepository.findAll().stream()
                .filter(token -> token.getUser().getId() == user.getId())
                .findFirst();
    }
}
