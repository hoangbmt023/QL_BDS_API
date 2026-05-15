package com.example.qlbds.auth_service.service;

public interface EmailService {
    void sendOtpEmail(String to, String otp);
    
    // Gửi email thông báo kết quả xét duyệt yêu cầu làm Agent
    void sendAgentRequestResultEmail(String to, String fullName, boolean approved, String adminNote);
}
