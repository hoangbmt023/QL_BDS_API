package com.example.qlbds.auth_service.service.impl;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.qlbds.auth_service.dto.*;
import com.example.qlbds.auth_service.entity.RefreshToken;
import com.example.qlbds.auth_service.model.Otp;
import com.example.qlbds.auth_service.repository.OtpRepository;
import com.example.qlbds.auth_service.repository.RefreshTokenRepository;
import com.example.qlbds.auth_service.service.AuthService;
import com.example.qlbds.auth_service.service.EmailService;
import com.example.qlbds.auth_service.service.OtpGenerator;
import com.example.qlbds.auth_service.service.OtpRateLimiter;
import com.example.qlbds.common.exception.InvalidResourceException;
import com.example.qlbds.common.exception.ResourceNotFoundException;
import com.example.qlbds.config.CustomUserDetailsService;
import com.example.qlbds.config.JwtService;
import com.example.qlbds.shared.entity.enums.UserRole;
import com.example.qlbds.user_service.entity.User;
import com.example.qlbds.user_service.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final OtpRepository otpRepository;
    private final OtpRateLimiter otpRateLimiter;
    private final OtpGenerator otpGenerator;

    // Đăng ký tài khoản người dùng mới
    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Tên đăng nhập '" + request.username() + "' đã được sử dụng");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email '" + request.email() + "' đã được đăng ký");
        }

        User newUser = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .phone(request.phone())
                .role(UserRole.USER)
                .isActive(false) // Account pending activation
                .build();

        userRepository.save(newUser);
        log.info("Đăng ký tài khoản thành công (đang chờ kích hoạt): {}", request.username());
    }

    // Đăng nhập và tạo JWT token
    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = findByUsername(request.username());

        if (user.isPending()) {
            throw new IllegalStateException("Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu không chính xác");
        }

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.username());
        
        // Tạo access token
        String accessToken = jwtService.generateToken(userDetails, user.getEmail(), user.getRole().name());

        // Nếu có trên 5 refresh token thì xóa token cũ nhất
        if (refreshTokenRepository.countByUser(user) >= 5) {
            refreshTokenRepository.findOldestByUserId(user.getId())
                    .ifPresent(token -> refreshTokenRepository.deleteById(token.getId()));
        }

        // Tạo refresh token
        String refreshTokenString = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenString)
                .expiryDate(Instant.now().plusSeconds(7 * 24 * 60 * 60)) // 7 days
                .build();
        
        refreshTokenRepository.save(refreshToken);

        log.info("Đăng nhập thành công: {}", request.username());
        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    // Làm mới access token bằng refresh token
    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Refresh token không hợp lệ"));

        if (token.isExpired()) {
            refreshTokenRepository.deleteById(token.getId());
            throw new IllegalArgumentException("Refresh token đã hết hạn, vui lòng đăng nhập lại");
        }

        User user = token.getUser();
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
        String accessToken = jwtService.generateToken(userDetails, user.getEmail(), user.getRole().name());

        return new AuthResponse(accessToken);
    }

    // Thu hồi refresh token (vô hiệu hóa token cụ thể)
    @Override
    @Transactional
    public void revokeToken(RevokeTokenRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Refresh token không tồn tại"));
        
        refreshTokenRepository.deleteById(token.getId());
        log.info("Đã thu hồi refresh token của user: {}", token.getUser().getUsername());
    }

    // Đăng xuất và xóa refresh token
    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Refresh token không tồn tại"));
        
        refreshTokenRepository.deleteById(token.getId());
        log.info("Đăng xuất thành công");
    }

    // Tạo và gửi mã OTP qua email để khôi phục mật khẩu
    @Override
    public void generateAndSendForgotPasswordOtpEmail(ForgotPasswordRequest request) {
        User user = findByEmail(request.email());

        if (user.isPending()) {
            throw new IllegalStateException("Tài khoản chưa được kích hoạt");
        }

        if (!otpRateLimiter.canSend(request.email())) {
            throw new IllegalStateException("Vui lòng đợi 60 giây trước khi gửi lại OTP");
        }

        String otpCode = otpGenerator.generateOtp(6);
        Otp otp = Otp.create(otpCode, 300); // 5 minutes
        otpRepository.save(request.email(), otp);

        emailService.sendOtpEmail(request.email(), otpCode);
        otpRateLimiter.recordSend(request.email());
    }

    // Xác minh mã OTP khôi phục mật khẩu
    @Override
    public void verifyForgotPassword(VerifyForgotPasswordRequest request) {
        findByEmail(request.email());
        
        Otp otp = otpRepository.get(request.email());

        if (otp == null) {
            throw new IllegalArgumentException("Mã OTP không hợp lệ hoặc đã hết hạn");
        }

        otp.verify(request.otp());
    }

    // Đặt lại mật khẩu mới sau khi xác minh OTP thành công
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = findByEmail(request.email());
        Otp otp = otpRepository.get(request.email());

        if (otp == null) {
            throw new InvalidResourceException("Mã OTP","không hợp lệ hoặc đã hết hạn");
        }

        otp.verify(request.otp());
        otpRepository.remove(request.email());

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        if (request.logoutAllDevices()) {
            refreshTokenRepository.deleteByUser(user);
        }
    }

    // Tạo và gửi mã OTP qua email để kích hoạt tài khoản
    @Override
    public void generateAndSendActivateOtpEmail(SendActivateOtpRequest request) {
        User user = findByEmail(request.email());

        if (!user.isPending()) {
            throw new IllegalStateException("Tài khoản đã được kích hoạt");
        }

        if (!otpRateLimiter.canSend(request.email())) {
            throw new IllegalStateException("Vui lòng đợi 60 giây trước khi gửi lại OTP");
        }

        String otpCode = otpGenerator.generateOtp(6);
        Otp otp = Otp.create(otpCode, 300);
        otpRepository.save(request.email(), otp);

        emailService.sendOtpEmail(request.email(), otpCode);
        otpRateLimiter.recordSend(request.email());
    }

    // Kích hoạt tài khoản bằng mã OTP
    @Override
    @Transactional
    public void activateAccount(ActivateAccountRequest request) {
        User user = findByEmail(request.email());
        Otp otp = otpRepository.get(request.email());

        if (otp == null) {
            throw new IllegalArgumentException("Mã OTP không hợp lệ hoặc đã hết hạn");
        }

        otp.verify(request.otp());
        otpRepository.remove(request.email());

        user.activate();
        userRepository.save(user);
    }

    // ==================== Private function ====================
    // Tìm kiếm người dùng theo email
    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với email: " + email));
    }

    // Tìm kiếm người dùng theo username
    private User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với username: " + username));
    }
}
