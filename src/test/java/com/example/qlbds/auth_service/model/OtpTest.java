package com.example.qlbds.auth_service.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.example.qlbds.common.exception.InvalidResourceException;

class OtpTest {

    @Test
    void create_ShouldInitializeCorrectly() {
        Otp otp = Otp.create("123456", 300);
        assertEquals("123456", otp.getCode());
        assertTrue(otp.getExpiryDate().isAfter(Instant.now()));
    }

    @Test
    void verify_ShouldNotThrowIfValid() {
        Otp otp = Otp.create("123456", 300);
        assertDoesNotThrow(() -> otp.verify("123456"));
    }

    @Test
    void verify_ShouldThrowIfCodeIsWrong() {
        Otp otp = Otp.create("123456", 300);
        InvalidResourceException exception = assertThrows(InvalidResourceException.class, () -> otp.verify("654321"));
        assertTrue(exception.getMessage().contains("không chính xác"));
    }

    @Test
    void verify_ShouldThrowIfExpired() {
        Otp otp = Otp.create("123456", -10); // Expired 10 seconds ago
        InvalidResourceException exception = assertThrows(InvalidResourceException.class, () -> otp.verify("123456"));
        assertTrue(exception.getMessage().contains("đã hết hạn"));
    }
}
