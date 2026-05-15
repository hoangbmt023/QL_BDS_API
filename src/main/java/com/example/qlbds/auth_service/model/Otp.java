package com.example.qlbds.auth_service.model;

import java.time.Instant;

import lombok.Getter;

@Getter
public class Otp {
    private final String code;
    private final Instant expiryDate;

    private Otp(String code, long expireInSeconds) {
        this.code = code;
        this.expiryDate = Instant.now().plusSeconds(expireInSeconds);
    }

    public static Otp create(String code, long expireInSeconds) {
        return new Otp(code, expireInSeconds);
    }

    public void verify(String inputCode) {
        if (expiryDate.isBefore(Instant.now())) {
            throw new IllegalArgumentException("Mã OTP đã hết hạn");
        }
        if (!this.code.equals(inputCode)) {
            throw new IllegalArgumentException("Mã OTP không chính xác");
        }
    }
}
