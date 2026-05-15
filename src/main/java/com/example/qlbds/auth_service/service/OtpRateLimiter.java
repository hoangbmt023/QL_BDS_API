package com.example.qlbds.auth_service.service;

public interface OtpRateLimiter {
    boolean canSend(String email);
    void recordSend(String email);
}
