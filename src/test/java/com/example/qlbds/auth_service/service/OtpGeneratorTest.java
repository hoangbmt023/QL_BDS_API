package com.example.qlbds.auth_service.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OtpGeneratorTest {

    @InjectMocks
    private OtpGenerator otpGenerator;

    @Test
    void generateOtp_ShouldReturnOtpOfSpecifiedLength() {
        int length = 6;
        String otp = otpGenerator.generateOtp(length);
        
        assertNotNull(otp);
        assertEquals(length, otp.length());
        assertTrue(otp.matches("\\d+"));
    }

    @Test
    void generateOtp_ShouldReturnDifferentOtps() {
        String otp1 = otpGenerator.generateOtp(6);
        String otp2 = otpGenerator.generateOtp(6);
        
        // Very low probability of collision
        assertNotEquals(otp1, otp2);
    }
}
