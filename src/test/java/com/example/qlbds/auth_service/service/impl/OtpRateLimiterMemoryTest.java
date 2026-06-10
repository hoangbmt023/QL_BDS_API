package com.example.qlbds.auth_service.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OtpRateLimiterMemoryTest {

    private OtpRateLimiterMemory otpRateLimiterMemory;

    @BeforeEach
    void setUp() {
        otpRateLimiterMemory = new OtpRateLimiterMemory();
    }

    @Test
    void canSend_ShouldReturnTrueIfNeverSent() {
        assertTrue(otpRateLimiterMemory.canSend("test@example.com"));
    }

    @Test
    void recordSend_And_canSend_ShouldReturnFalseIfWithin60Seconds() {
        String email = "test@example.com";
        otpRateLimiterMemory.recordSend(email);
        
        assertFalse(otpRateLimiterMemory.canSend(email));
    }
}
