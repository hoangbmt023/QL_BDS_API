package com.example.qlbds.auth_service.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class OtpGenerator {
    private static final SecureRandom random = new SecureRandom();

    public String generateOtp(int length) {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}
