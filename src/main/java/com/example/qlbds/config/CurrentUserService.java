package com.example.qlbds.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.qlbds.common.exception.ResourceNotFoundException;
import com.example.qlbds.user_service.entity.User;
import com.example.qlbds.user_service.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service lấy thông tin User đang đăng nhập từ SecurityContext.
 */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    /**
     * Lấy User entity của người dùng đang đăng nhập.
     *
     * @throws ResourceNotFoundException nếu không tìm thấy user trong DB
     * @throws IllegalStateException     nếu chưa đăng nhập
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalStateException("No authenticated user found");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    /**
     * Lấy username của người dùng đang đăng nhập.
     */
    public String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }
}
