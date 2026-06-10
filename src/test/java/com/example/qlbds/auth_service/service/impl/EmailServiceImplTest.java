package com.example.qlbds.auth_service.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.example.qlbds.common.exception.InvalidResourceException;

import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void sendOtpEmail_ShouldSendEmailSuccessfully() {
        String to = "test@example.com";
        String otp = "123456";
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(templateEngine.process(eq("email/otp-email"), any(Context.class))).thenReturn("<html>OTP</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendOtpEmail(to, otp);

        verify(templateEngine).process(eq("email/otp-email"), any(Context.class));
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendAgentRequestResultEmail_Approved_ShouldSendEmailSuccessfully() {
        String to = "agent@example.com";
        String fullName = "Agent Name";
        boolean approved = true;
        String adminNote = "Welcome";
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(templateEngine.process(eq("email/agent-request-result"), any(Context.class))).thenReturn("<html>Approved</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendAgentRequestResultEmail(to, fullName, approved, adminNote);

        verify(templateEngine).process(eq("email/agent-request-result"), any(Context.class));
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendAgentRequestResultEmail_Rejected_ShouldSendEmailSuccessfully() {
        String to = "agent@example.com";
        String fullName = "Agent Name";
        boolean approved = false;
        String adminNote = "Rejected note";
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(templateEngine.process(eq("email/agent-request-result"), any(Context.class))).thenReturn("<html>Rejected</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendAgentRequestResultEmail(to, fullName, approved, adminNote);

        verify(templateEngine).process(eq("email/agent-request-result"), any(Context.class));
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendHtmlEmail_ShouldThrowExceptionWhenMailSenderThrows() {
        String to = "test@example.com";
        String otp = "123456";
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(templateEngine.process(eq("email/otp-email"), any(Context.class))).thenReturn("<html>OTP</html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailException("Error") {}).when(mailSender).send(mimeMessage);

        assertThrows(InvalidResourceException.class, () -> emailService.sendOtpEmail(to, otp));
    }

    @Test
    void sendHtmlEmail_ShouldThrowExceptionWhenTemplateEngineThrows() {
        String to = "test@example.com";
        String otp = "123456";

        when(templateEngine.process(eq("email/otp-email"), any(Context.class)))
                .thenThrow(new org.thymeleaf.exceptions.TemplateProcessingException("Template error"));

        assertThrows(org.thymeleaf.exceptions.TemplateProcessingException.class, () -> emailService.sendOtpEmail(to, otp));
    }
}
