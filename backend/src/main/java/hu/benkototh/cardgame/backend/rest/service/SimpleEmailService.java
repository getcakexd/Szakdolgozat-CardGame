package hu.benkototh.cardgame.backend.rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import hu.benkototh.cardgame.backend.rest.controller.AuditLogController;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class SimpleEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.email.from.name:Card Game Support}")
    private String fromName;

    @Value("${app.url:http://localhost:4200}")
    private String appUrl;

    @Autowired
    private AuditLogController auditLogController;


    public void sendPasswordResetEmail(String to, String token, long userId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Password Reset Request");

            String resetUrl = appUrl + "/reset-password?token=" + token;
            String htmlContent = createPasswordResetEmailContent(resetUrl);

            helper.setText(htmlContent, true);

            mailSender.send(message);

            auditLogController.logAction("EMAIL_SENT", userId,
                    "Password reset email sent to: " + to);

        } catch (MessagingException e) {
            auditLogController.logAction("EMAIL_SEND_FAILED", userId,
                    "Failed to send password reset email: " + e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String createPasswordResetEmailContent(String resetUrl) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                "<h2 style='color: #333;'>Password Reset Request</h2>" +
                "<p>You have requested to reset your password. Please click the link below to set a new password:</p>" +
                "<p><a href='" + resetUrl + "' style='display: inline-block; padding: 10px 20px; background-color: #4285f4; color: white; text-decoration: none; border-radius: 4px;'>Reset Password</a></p>" +
                "<p>If you did not request a password reset, please ignore this email or contact support if you have concerns.</p>" +
                "<p>This link will expire in 24 hours.</p>" +
                "<p>Thank you,<br>The Card Game Team</p>" +
                "</div>";
    }

    public void sendVerificationEmail(String to, String token, long userId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Email Verification");

            String verificationUrl = appUrl + "/verify-email?token=" + token;
            String htmlContent = createVerificationEmailContent(verificationUrl);

            helper.setText(htmlContent, true);

            mailSender.send(message);

            auditLogController.logAction("EMAIL_SENT", userId,
                    "Verification email sent to: " + to);

        } catch (MessagingException e) {
            auditLogController.logAction("EMAIL_SEND_FAILED", userId,
                    "Failed to send verification email: " + e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String createVerificationEmailContent(String verificationUrl) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                "<h2 style='color: #333;'>Email Verification</h2>" +
                "<p>Thank you for registering! Please click the link below to verify your email address:</p>" +
                "<p><a href='" + verificationUrl + "' style='display: inline-block; padding: 10px 20px; background-color: #4285f4; color: white; text-decoration: none; border-radius: 4px;'>Verify Email</a></p>" +
                "<p>If you did not create an account, please ignore this email.</p>" +
                "<p>This link will expire in 24 hours.</p>" +
                "<p>Thank you,<br>The Card Game Team</p>" +
                "</div>";
    }

    public void sendSimpleEmail(String to, String subject, String text, long userId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            mailSender.send(message);

            auditLogController.logAction("EMAIL_SENT", userId,
                    "Email sent to: " + to);

        } catch (MessagingException e) {
            auditLogController.logAction("EMAIL_SEND_FAILED", userId,
                    "Failed to send email: " + e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
