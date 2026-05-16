package com.example.qlbds.user_service.service;

import java.util.List;

import com.example.qlbds.common.exception.ResourceNotFoundException;
import com.example.qlbds.user_service.dto.AdminChangeRoleRequest;
import com.example.qlbds.user_service.dto.UserProfileResponse;
import com.example.qlbds.user_service.dto.UserSummaryResponse;

public interface UserService {

    // Lấy danh sách tất cả users (dành cho ADMIN).
    List<UserSummaryResponse> getAllUsers();

    // Đổi role của một user với đầy đủ nghiệp vụ (dành cho ADMIN).
    UserSummaryResponse changeRole(Long userId, AdminChangeRoleRequest request) throws ResourceNotFoundException;

    // Lấy thông tin profile của user đang đăng nhập.
    UserProfileResponse me() throws ResourceNotFoundException;

    // Cập nhật thông tin profile của user đang đăng nhập.
    UserProfileResponse updateProfile(com.example.qlbds.user_service.dto.UpdateProfileRequest request)
            throws ResourceNotFoundException;

    // Xóa user (soft-delete).
    void deleteUser(Long userId) throws ResourceNotFoundException;

    // Khôi phục user đã bị xóa mềm (dành cho ADMIN).
    void restoreUser(Long userId) throws ResourceNotFoundException;
}
