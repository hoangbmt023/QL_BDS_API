package com.example.qlbds.user_service.service;

import java.util.List;

import com.example.qlbds.common.exception.ResourceNotFoundException;
import com.example.qlbds.user_service.dto.ChangeUserRoleRequest;
import com.example.qlbds.user_service.dto.UserProfileResponse;
import com.example.qlbds.user_service.dto.UserSummaryResponse;

/**
 * UserService quản lý thông tin người dùng.
 * Logic xác thực (đăng ký / đăng nhập) đã được tách sang AuthService.
 */
public interface UserService {

    /**
     * Lấy danh sách tất cả users (dành cho ADMIN).
     */
    List<UserSummaryResponse> getAllUsers();

    /**
     * Đổi role của một user (dành cho ADMIN).
     */
    UserSummaryResponse changeRole(Long userId, ChangeUserRoleRequest request) throws ResourceNotFoundException;

    /**
     * Lấy thông tin profile của user đang đăng nhập.
     */
    UserProfileResponse me() throws ResourceNotFoundException;
}
