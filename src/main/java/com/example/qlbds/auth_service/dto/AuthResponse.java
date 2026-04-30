package com.example.qlbds.auth_service.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType
) {
    /** Tạo AuthResponse với tokenType mặc định là "Bearer" */
    public AuthResponse(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, "Bearer");
    }

    public AuthResponse(String accessToken) {
        this(accessToken, null, "Bearer");
    }
}
