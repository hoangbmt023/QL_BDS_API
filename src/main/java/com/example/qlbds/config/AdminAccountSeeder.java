package com.example.qlbds.config;

import com.example.qlbds.shared.entity.enums.UserRole;
import com.example.qlbds.user_service.entity.User;
import com.example.qlbds.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Cho phép cấu hình qua biến môi trường (Ví dụ thêm ADMIN_USERNAME vào .env)
    @Value("${admin.default.username}")
    private String defaultUsername;

    @Value("${admin.default.password}")
    private String defaultPassword;

    @Value("${admin.default.email}")
    private String defaultEmail;

    @Override
    @Transactional
    public void run(String... args) {
        // Kiểm tra xem đã có tài khoản admin mặc định chưa
        if (!userRepository.existsByUsername(defaultUsername)) {
            log.info("Bắt đầu khởi tạo tài khoản Admin mặc định...");

            User admin = User.builder()
                    .username(defaultUsername)
                    .password(passwordEncoder.encode(defaultPassword))
                    .email(defaultEmail)
                    .fullName("System Administrator")
                    .role(UserRole.ADMIN)
                    .isActive(true)
                    .isDeleted(false)
                    .build();

            userRepository.save(admin);
            log.info("Tạo tài khoản Admin thành công! Username: {} - Password: {}", defaultUsername, defaultPassword);
        } else {
            log.info("Tài khoản Admin '{}' đã tồn tại. Bỏ qua bước khởi tạo.", defaultUsername);
        }
    }
}
