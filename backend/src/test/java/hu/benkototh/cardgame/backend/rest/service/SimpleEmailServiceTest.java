package hu.benkototh.cardgame.backend.rest.service;

import hu.benkototh.cardgame.backend.rest.controller.AuditLogController;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private AuditLogController auditLogController;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    @Spy
    private SimpleEmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@example.com");
        ReflectionTestUtils.setField(emailService, "fromName", "Card Game Support");
        ReflectionTestUtils.setField(emailService, "appUrl", "http://localhost:4200");
    }

    @Test
    void testSendPasswordResetEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendPasswordResetEmail("test@example.com", "test-token", 1L);

        verify(mailSender).send(mimeMessage);
        verify(auditLogController).logAction(eq("EMAIL_SENT"), eq(1L), anyString());
    }

    @Test
    void testSendPasswordResetEmailException() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("Email error")).when(mailSender).send(any(MimeMessage.class));

        try {
            emailService.sendPasswordResetEmail("test@example.com", "test-token", 1L);
            fail("Expected RuntimeException was not thrown");
        } catch (RuntimeException e) {

        }
    }

    @Test
    void testSendVerificationEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendVerificationEmail("test@example.com", "test-token", 1L);

        verify(mailSender).send(mimeMessage);
        verify(auditLogController).logAction(eq("EMAIL_SENT"), eq(1L), anyString());
    }

    @Test
    void testSendVerificationEmailException() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("Email error")).when(mailSender).send(any(MimeMessage.class));

        try {
            emailService.sendVerificationEmail("test@example.com", "test-token", 1L);
            fail("Expected RuntimeException was not thrown");
        } catch (RuntimeException e) {
        }
    }

    @Test
    void testSendSimpleEmail() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendSimpleEmail("test@example.com", "Test Subject", "Test Content", 1L);

        verify(mailSender).send(mimeMessage);
        verify(auditLogController).logAction(eq("EMAIL_SENT"), eq(1L), anyString());
    }

    @Test
    void testSendSimpleEmailException() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("Email error")).when(mailSender).send(any(MimeMessage.class));

        try {
            emailService.sendSimpleEmail("test@example.com", "Test Subject", "Test Content", 1L);
            fail("Expected RuntimeException was not thrown");
        } catch (RuntimeException e) {
        }
    }
}