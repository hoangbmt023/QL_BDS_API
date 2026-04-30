package com.example.qlbds.user_service.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.qlbds.common.exception.ResourceNotFoundException;
import com.example.qlbds.config.CurrentUserService;
import com.example.qlbds.user_service.dto.ChangeUserRoleRequest;
import com.example.qlbds.user_service.dto.UserProfileResponse;
import com.example.qlbds.user_service.dto.UserSummaryResponse;
import com.example.qlbds.user_service.entity.User;
import com.example.qlbds.user_service.repository.UserRepository;
import com.example.qlbds.user_service.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    // ==================== Queries ====================

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse me() throws ResourceNotFoundException {
        User user = currentUserService.getCurrentUser();
        return toUserProfileResponse(user);
    }

    // ==================== Commands ====================

    @Override
    @Transactional
    public UserSummaryResponse changeRole(Long userId, ChangeUserRoleRequest request)
            throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy người dùng với ID: " + userId));

        user.setRole(request.role());
        User saved = userRepository.save(user);

        log.info("Đã đổi role người dùng [{}] sang {}", userId, request.role());
        return toUserSummaryResponse(saved);
    }

    // ==================== Mappers ====================

    private UserSummaryResponse toUserSummaryResponse(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getIsActive()
        );
    }

    private UserProfileResponse toUserProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole(),
                user.getIsActive()
        );
    }
}
