package com.example.qlbds.auth_service.service.impl;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.example.qlbds.auth_service.service.EmailService;
import com.example.qlbds.common.exception.InvalidResourceException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;

@Slf4j
@Service
@RequiredArgsConstructor
@Async
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    public void sendOtpEmail(String to, String otp) {
        Context context = new Context();
        context.setVariable("otp", otp);
        context.setVariable("expire", 5);

        String html = templateEngine.process("email/otp-email", context);

        sendHtmlEmail(to, "Mã OTP xác thực của bạn", html);
    }

    @Override
    public void sendAgentRequestResultEmail(String to, String fullName, boolean approved, String adminNote) {
        Context context = new Context();
        context.setVariable("fullName", fullName);
        context.setVariable("approved", approved);
        context.setVariable("adminNote", adminNote);

        String html = templateEngine.process("email/agent-request-result", context);
        String subject = approved ? "Yêu cầu làm môi giới của bạn đã được DUYỆT" : "Yêu cầu làm môi giới của bạn bị TỪ CHỐI";

        sendHtmlEmail(to, subject, html);
    }

    private void sendHtmlEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Đã gửi email đến: {}", to);
        } catch (MessagingException | MailException e) {
            log.error("Lỗi khi gửi email đến {}", to, e);
            throw new InvalidResourceException("Email", "Gửi email thất bại đến " + to);
        }
    }
}
