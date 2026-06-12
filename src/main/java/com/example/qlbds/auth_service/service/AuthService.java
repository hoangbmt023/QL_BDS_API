package com.example.qlbds.auth_service.service;

import com.example.qlbds.auth_service.dto.*;

/**
 * AuthService xử lý xác thực người dùng.
 */
public interface AuthService {

    void register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void revokeToken(RevokeTokenRequest request);

    void logout(LogoutRequest request);

    void generateAndSendForgotPasswordOtpEmail(ForgotPasswordRequest request);

    void verifyForgotPassword(VerifyForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void generateAndSendActivateOtpEmail(SendActivateOtpRequest request);

    void activateAccount(ActivateAccountRequest request);

    void generateAndSendRestoreOtpEmail(SendRestoreOtpRequest request);

    void restoreAccount(RestoreAccountRequest request);
}
