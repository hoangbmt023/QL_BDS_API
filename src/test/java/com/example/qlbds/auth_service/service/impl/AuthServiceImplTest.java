package com.example.qlbds.auth_service.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.qlbds.auth_service.dto.*;
import com.example.qlbds.auth_service.entity.RefreshToken;
import com.example.qlbds.auth_service.model.Otp;
import com.example.qlbds.auth_service.repository.OtpRepository;
import com.example.qlbds.auth_service.repository.RefreshTokenRepository;
import com.example.qlbds.auth_service.service.EmailService;
import com.example.qlbds.auth_service.service.OtpGenerator;
import com.example.qlbds.auth_service.service.OtpRateLimiter;
import com.example.qlbds.common.exception.DuplicateResourceException;
import com.example.qlbds.common.exception.InvalidResourceException;
import com.example.qlbds.common.exception.ResourceNotFoundException;
import com.example.qlbds.config.CustomUserDetailsService;
import com.example.qlbds.config.JwtService;
import com.example.qlbds.shared.entity.enums.UserRole;
import com.example.qlbds.user_service.entity.User;
import com.example.qlbds.user_service.repository.UserRepository;
import com.example.qlbds.user_service.service.UserService;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CustomUserDetailsService customUserDetailsService;
    @Mock
    private JwtService jwtService;
    @Mock
    private EmailService emailService;
    @Mock
    private OtpRepository otpRepository;
    @Mock
    private OtpRateLimiter otpRateLimiter;
    @Mock
    private OtpGenerator otpGenerator;
    @Mock
    private UserService userService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User mockUser;
    private UserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .role(UserRole.USER)
                .isActive(true)
                .isDeleted(false)
                .build();

        mockUserDetails = mock(UserDetails.class);
    }

    // --- register ---
    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest("newuser", "new@example.com", "password", "New User",
                "0123456789");
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded_pwd");

        authService.register(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ThrowsWhenUsernameExists() {
        RegisterRequest request = new RegisterRequest("newuser", "new@example.com", "password", "New User",
                "0123456789");
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ThrowsWhenEmailExists() {
        RegisterRequest request = new RegisterRequest("newuser", "new@example.com", "password", "New User",
                "0123456789");
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    // --- login ---
    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("testuser", "password");
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(request.password(), mockUser.getPassword())).thenReturn(true);
        when(customUserDetailsService.loadUserByUsername(request.username())).thenReturn(mockUserDetails);
        when(jwtService.generateToken(mockUserDetails, mockUser.getEmail(), mockUser.getRole().name()))
                .thenReturn("access_token");
        when(refreshTokenRepository.countByUser(mockUser)).thenReturn(1L);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access_token", response.accessToken());
        assertNotNull(response.refreshToken());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_ThrowsWhenUserDeleted() {
        LoginRequest request = new LoginRequest("testuser", "password");
        mockUser.setIsDeleted(true);
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(mockUser));

        assertThrows(InvalidResourceException.class, () -> authService.login(request));
    }

    @Test
    void login_ThrowsWhenUserPending() {
        LoginRequest request = new LoginRequest("testuser", "password");
        mockUser.setIsActive(false);
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(mockUser));

        assertThrows(InvalidResourceException.class, () -> authService.login(request));
    }

    @Test
    void login_ThrowsWhenWrongPassword() {
        LoginRequest request = new LoginRequest("testuser", "wrong_password");
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(request.password(), mockUser.getPassword())).thenReturn(false);

        assertThrows(InvalidResourceException.class, () -> authService.login(request));
    }

    @Test
    void login_ThrowsWhenUsernameNotFound() {
        LoginRequest request = new LoginRequest("testuser", "password");
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.login(request));
    }

    @Test
    void login_DeletesOldestRefreshTokenWhenCountExceedsLimit() {
        LoginRequest request = new LoginRequest("testuser", "password");
        when(userRepository.findByUsername(request.username())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(request.password(), mockUser.getPassword())).thenReturn(true);
        when(customUserDetailsService.loadUserByUsername(request.username())).thenReturn(mockUserDetails);
        when(jwtService.generateToken(mockUserDetails, mockUser.getEmail(), mockUser.getRole().name()))
                .thenReturn("access_token");
        when(refreshTokenRepository.countByUser(mockUser)).thenReturn(5L);
        RefreshToken oldestToken = RefreshToken.builder().id(10L).build();
        when(refreshTokenRepository.findOldestByUserId(mockUser.getId())).thenReturn(Optional.of(oldestToken));

        authService.login(request);

        verify(refreshTokenRepository).deleteById(10L);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    // --- refreshToken ---
    @Test
    void refreshToken_Success() {
        RefreshTokenRequest request = new RefreshTokenRequest("valid_refresh_token");
        RefreshToken refreshToken = RefreshToken.builder().id(1L).user(mockUser).token("valid_refresh_token")
                .expiryDate(Instant.now().plusSeconds(3600)).build();
        when(refreshTokenRepository.findByToken(request.refreshToken())).thenReturn(Optional.of(refreshToken));
        when(customUserDetailsService.loadUserByUsername(mockUser.getUsername())).thenReturn(mockUserDetails);
        when(jwtService.generateToken(mockUserDetails, mockUser.getEmail(), mockUser.getRole().name()))
                .thenReturn("new_access_token");

        AuthResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals("new_access_token", response.accessToken());
        assertNull(response.refreshToken()); // the response only has accessToken
    }

    @Test
    void refreshToken_ThrowsWhenNotFound() {
        RefreshTokenRequest request = new RefreshTokenRequest("invalid_refresh_token");
        when(refreshTokenRepository.findByToken(request.refreshToken())).thenReturn(Optional.empty());

        assertThrows(InvalidResourceException.class, () -> authService.refreshToken(request));
    }

    @Test
    void refreshToken_ThrowsWhenExpired() {
        RefreshTokenRequest request = new RefreshTokenRequest("expired_refresh_token");
        RefreshToken refreshToken = RefreshToken.builder().id(1L).user(mockUser).token("expired_refresh_token")
                .expiryDate(Instant.now().minusSeconds(3600)).build();
        when(refreshTokenRepository.findByToken(request.refreshToken())).thenReturn(Optional.of(refreshToken));

        assertThrows(InvalidResourceException.class, () -> authService.refreshToken(request));
        verify(refreshTokenRepository).deleteById(1L);
    }

    // --- revokeToken ---
    @Test
    void revokeToken_Success() {
        RevokeTokenRequest request = new RevokeTokenRequest("valid_refresh_token");
        RefreshToken refreshToken = RefreshToken.builder().id(1L).user(mockUser).token("valid_refresh_token").build();
        when(refreshTokenRepository.findByToken(request.refreshToken())).thenReturn(Optional.of(refreshToken));

        authService.revokeToken(request);

        verify(refreshTokenRepository).deleteById(1L);
    }

    @Test
    void revokeToken_ThrowsWhenNotFound() {
        RevokeTokenRequest request = new RevokeTokenRequest("invalid_refresh_token");
        when(refreshTokenRepository.findByToken(request.refreshToken())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.revokeToken(request));
    }

    // --- logout ---
    @Test
    void logout_Success() {
        LogoutRequest request = new LogoutRequest("valid_refresh_token");
        RefreshToken refreshToken = RefreshToken.builder().id(1L).user(mockUser).token("valid_refresh_token").build();
        when(refreshTokenRepository.findByToken(request.refreshToken())).thenReturn(Optional.of(refreshToken));

        authService.logout(request);

        verify(refreshTokenRepository).deleteById(1L);
    }

    @Test
    void logout_ThrowsWhenNotFound() {
        LogoutRequest request = new LogoutRequest("invalid_refresh_token");
        when(refreshTokenRepository.findByToken(request.refreshToken())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.logout(request));
    }

    // --- generateAndSendForgotPasswordOtpEmail ---
    @Test
    void generateAndSendForgotPasswordOtpEmail_Success() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");
        when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.of(mockUser));
        when(otpRateLimiter.canSend(request.email())).thenReturn(true);
        when(otpGenerator.generateOtp(6)).thenReturn("123456");

        authService.generateAndSendForgotPasswordOtpEmail(request);

        verify(otpRepository).save(eq(request.email()), any(Otp.class));
        verify(emailService).sendOtpEmail(request.email(), "123456");
        verify(otpRateLimiter).recordSend(request.email());
    }

    @Test
    void generateAndSendForgotPasswordOtpEmail_ThrowsWhenPending() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");
        mockUser.setIsActive(false);
        when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.of(mockUser));

        assertThrows(InvalidResourceException.class, () -> authService.generateAndSendForgotPasswordOtpEmail(request));
    }

    @Test
    void generateAndSendForgotPasswordOtpEmail_ThrowsWhenRateLimited() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");
        when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.of(mockUser));
        when(otpRateLimiter.canSend(request.email())).thenReturn(false);

        assertThrows(InvalidResourceException.class, () -> authService.generateAndSendForgotPasswordOtpEmail(request));
    }

    @Test
    void generateAndSendForgotPasswordOtpEmail_ThrowsWhenEmailNotFound() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("notfound@example.com");
        when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.generateAndSendForgotPasswordOtpEmail(request));
    }

    // --- verifyForgotPassword ---
    @Test
    void verifyForgotPassword_Success() {
        VerifyForgotPasswordRequest request = new VerifyForgotPasswordRequest("test@example.com", "123456");
        when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.of(mockUser));
        Otp otp = Otp.create("123456", 300);
        when(otpRepository.get(request.email())).thenReturn(otp);

        assertDoesNotThrow(() -> authService.verifyForgotPassword(request));
    }

    @Test
    void verifyForgotPassword_ThrowsWhenOtpNull() {
        VerifyForgotPasswordRequest request = new VerifyForgotPasswordRequest("test@example.com", "123456");
        when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.of(mockUser));
        when(otpRepository.get(request.email())).thenReturn(null);

        assertThrows(InvalidResourceException.class, () -> authService.verifyForgotPassword(request));
    }

    @Test
    void verifyForgotPassword_ThrowsWhenOtpWrong() {
        VerifyForgotPasswordRequest request = new VerifyForgotPasswordRequest("test@example.com", "wrong");
        when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.of(mockUser));
        Otp otp = Otp.create("123456", 300);
        when(otpRepository.get(request.email())).thenReturn(otp);

        assertThrows(InvalidResourceException.class, () -> authService.verifyForgotPassword(request));
    }

    @Test
    void verifyForgotPassword_ThrowsWhenEmailNotFound() {
        VerifyForgotPasswordRequest request = new VerifyForgotPasswordRequest("notfound@example.com", "123456");
        when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.verifyForgotPassword(request));
    }

    // --- resetPassword ---
    @Test
    void resetPassword_Success() {
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "123456", "new_password", false);
        when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.of(mockUser));
        Otp otp = Otp.create("123456", 300);
        when(otpRepository.get(request.email())).thenReturn(otp);
        when(passwordEncoder.encode(request.newPassword())).thenReturn("new_encoded_pwd");

        authService.resetPassword(request);

        verify(otpRepository).remove(request.email());
        verify(userRepository).save(mockUser);
        assertEquals("new_encoded_pwd", mockUser.getPassword());
        verify(refreshTokenRepository, never()).deleteByUser(mockUser);
    }

    @Test
    void resetPassword_SuccessLogoutAll() {
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "123456", "new_password", true);
        when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.of(mockUser));
        Otp otp = Otp.create("123456", 300);
        when(otpRepository.get(request.email())).thenReturn(otp);
        when(passwordEncoder.encode(request.newPassword())).thenReturn("new_encoded_pwd");

        authService.resetPassword(request);

        verify(refreshTokenRepository).deleteByUser(mockUser);
    }

    @Test
    void resetPassword_ThrowsWhenOtpNull() {
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com", "123456", "new_password", false);
        when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.of(mockUser));
        when(otpRepository.get(request.email())).thenReturn(null);

        assertThrows(InvalidResourceException.class, () -> authService.resetPassword(request));
    }

    @Test
    void resetPassword_ThrowsWhenEmailNotFound() {
        ResetPasswordRequest request = new ResetPasswordRequest("notfound@example.com", "123456", "new_password",
                false);
        when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.resetPassword(request));
    }

    // --- generateAndSendActivateOtpEmail ---
    @Test
    void generateAndSendActivateOtpEmail_Success() {
        SendActivateOtpRequest request = new SendActivateOtpRequest("test@example.com");
        mockUser.setIsActive(false);
        when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.of(mockUser));
        when(otpRateLimiter.canSend(request.email())).thenReturn(true);
        when(otpGenerator.generateOtp(6)).thenReturn("123456");

        authService.generateAndSendActivateOtpEmail(request);

        verify(otpRepository).save(eq(request.email()), any(Otp.class));
        verify(emailService).sendOtpEmail(request.email(), "123456");
        verify(otpRateLimiter).recordSend(request.email());
    }

    @Test
    void generateAndSendActivateOtpEmail_ThrowsWhenAlreadyActive() {
        SendActivateOtpRequest request = new SendActivateOtpRequest("test@example.com");
        mockUser.setIsActive(true);
        when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.of(mockUser));

        assertThrows(InvalidResourceException.class, () -> authService.generateAndSendActivateOtpEmail(request));
    }

    @Test
    void generateAndSendActivateOtpEmail_ThrowsWhenRateLimited() {
        SendActivateOtpRequest request = new SendActivateOtpRequest("test@example.com");
        mockUser.setIsActive(false);
        when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.of(mockUser));
        when(otpRateLimiter.canSend(request.email())).thenReturn(false);

        assertThrows(InvalidResourceException.class, () -> authService.generateAndSendActivateOtpEmail(request));
    }

    @Test
    void generateAndSendActivateOtpEmail_ThrowsWhenEmailNotFound() {
        SendActivateOtpRequest request = new SendActivateOtpRequest("notfound@example.com");
        when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.generateAndSendActivateOtpEmail(request));
    }

    // --- activateAccount ---
    @Test
    void activateAccount_Success() {
        ActivateAccountRequest request = new ActivateAccountRequest("test@example.com", "123456");
        mockUser.setIsActive(false);
        when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.of(mockUser));
        Otp otp = Otp.create("123456", 300);
        when(otpRepository.get(request.email())).thenReturn(otp);

        authService.activateAccount(request);

        verify(otpRepository).remove(request.email());
        verify(userRepository).save(mockUser);
        assertTrue(mockUser.getIsActive());
    }

    @Test
    void activateAccount_ThrowsWhenOtpNull() {
        ActivateAccountRequest request = new ActivateAccountRequest("test@example.com", "123456");
        when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.of(mockUser));
        when(otpRepository.get(request.email())).thenReturn(null);

        assertThrows(InvalidResourceException.class, () -> authService.activateAccount(request));
    }

    @Test
    void activateAccount_ThrowsWhenEmailNotFound() {
        ActivateAccountRequest request = new ActivateAccountRequest("notfound@example.com", "123456");
        when(userRepository.findByEmailAndIsDeletedFalse(request.email())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.activateAccount(request));
    }

    // --- generateAndSendRestoreOtpEmail ---
    @Test
    void generateAndSendRestoreOtpEmail_Success() {
        SendRestoreOtpRequest request = new SendRestoreOtpRequest("test@example.com");
        mockUser.setIsDeleted(true);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(mockUser));
        when(otpRateLimiter.canSend(request.email())).thenReturn(true);
        when(otpGenerator.generateOtp(6)).thenReturn("123456");

        authService.generateAndSendRestoreOtpEmail(request);

        verify(otpRepository).save(eq(request.email()), any(Otp.class));
        verify(emailService).sendOtpEmail(request.email(), "123456");
        verify(otpRateLimiter).recordSend(request.email());
    }

    @Test
    void generateAndSendRestoreOtpEmail_ThrowsWhenNotDeleted() {
        SendRestoreOtpRequest request = new SendRestoreOtpRequest("test@example.com");
        mockUser.setIsDeleted(false);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(mockUser));

        assertThrows(InvalidResourceException.class, () -> authService.generateAndSendRestoreOtpEmail(request));
    }

    @Test
    void generateAndSendRestoreOtpEmail_ThrowsWhenRateLimited() {
        SendRestoreOtpRequest request = new SendRestoreOtpRequest("test@example.com");
        mockUser.setIsDeleted(true);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(mockUser));
        when(otpRateLimiter.canSend(request.email())).thenReturn(false);

        assertThrows(InvalidResourceException.class, () -> authService.generateAndSendRestoreOtpEmail(request));
    }

    @Test
    void generateAndSendRestoreOtpEmail_ThrowsWhenUserNotFound() {
        SendRestoreOtpRequest request = new SendRestoreOtpRequest("test@example.com");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.generateAndSendRestoreOtpEmail(request));
    }

    // --- restoreAccount ---
    @Test
    void restoreAccount_Success() {
        RestoreAccountRequest request = new RestoreAccountRequest("test@example.com", "123456");
        mockUser.setIsDeleted(true);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(mockUser));
        Otp otp = Otp.create("123456", 300);
        when(otpRepository.get(request.email())).thenReturn(otp);

        authService.restoreAccount(request);

        verify(otpRepository).remove(request.email());
        verify(userService).restoreUser(mockUser.getId());
    }

    @Test
    void restoreAccount_ThrowsWhenNotDeleted() {
        RestoreAccountRequest request = new RestoreAccountRequest("test@example.com", "123456");
        mockUser.setIsDeleted(false);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(mockUser));

        assertThrows(InvalidResourceException.class, () -> authService.restoreAccount(request));
    }

    @Test
    void restoreAccount_ThrowsWhenOtpNull() {
        RestoreAccountRequest request = new RestoreAccountRequest("test@example.com", "123456");
        mockUser.setIsDeleted(true);
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(mockUser));
        when(otpRepository.get(request.email())).thenReturn(null);

        assertThrows(InvalidResourceException.class, () -> authService.restoreAccount(request));
    }

    @Test
    void restoreAccount_ThrowsWhenUserNotFound() {
        RestoreAccountRequest request = new RestoreAccountRequest("test@example.com", "123456");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.restoreAccount(request));
    }
}
