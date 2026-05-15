package com.example.qlbds.auth_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.qlbds.auth_service.dto.*;
import com.example.qlbds.auth_service.service.AuthService;
import com.example.qlbds.shared.dto.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Các endpoint xác thực công khai (không cần JWT).
 * Được permit trong SecurityConfiguration: /api/auth/**
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Xác thực", description = "Đăng ký, đăng nhập và quản lý tài khoản")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản mới (cần kích hoạt)")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<Void>builder()
                        .success(true)
                        .message("Đăng ký thành công. Vui lòng gửi yêu cầu OTP để kích hoạt tài khoản")
                        .build());
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập và nhận JWT token cùng refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Đăng nhập thành công"));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Làm mới access token bằng refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse authResponse = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Làm mới access token thành công"));
    }

    @PostMapping("/revoke-token")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thu hồi refresh token (dành cho Admin)")
    public ResponseEntity<ApiResponse<Void>> revokeToken(@Valid @RequestBody RevokeTokenRequest request) {
        authService.revokeToken(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Thu hồi refresh token thành công").build());
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất và thu hồi refresh token")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Đăng xuất thành công").build());
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Gửi OTP quên mật khẩu qua email")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.generateAndSendForgotPasswordOtpEmail(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Mã OTP đã được gửi đến email").build());
    }

    @PostMapping("/verify-forgot-password")
    @Operation(summary = "Xác thực OTP quên mật khẩu")
    public ResponseEntity<ApiResponse<Void>> verifyForgotPassword(@Valid @RequestBody VerifyForgotPasswordRequest request) {
        authService.verifyForgotPassword(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Mã OTP hợp lệ").build());
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Đặt lại mật khẩu mới")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Đặt lại mật khẩu thành công").build());
    }

    @PostMapping("/send-activate-otp")
    @Operation(summary = "Gửi OTP kích hoạt tài khoản qua email")
    public ResponseEntity<ApiResponse<Void>> sendActivateOtp(@Valid @RequestBody SendActivateOtpRequest request) {
        authService.generateAndSendActivateOtpEmail(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Mã OTP kích hoạt đã được gửi đến email").build());
    }

    @PostMapping("/activate-account")
    @Operation(summary = "Kích hoạt tài khoản bằng mã OTP")
    public ResponseEntity<ApiResponse<Void>> activateAccount(@Valid @RequestBody ActivateAccountRequest request) {
        authService.activateAccount(request);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Kích hoạt tài khoản thành công").build());
    }
}
