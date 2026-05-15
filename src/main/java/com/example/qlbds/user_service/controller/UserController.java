package com.example.qlbds.user_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.qlbds.common.exception.ResourceNotFoundException;
import com.example.qlbds.shared.dto.ApiResponse;
import com.example.qlbds.user_service.dto.ChangeUserRoleRequest;
import com.example.qlbds.user_service.dto.UserProfileResponse;
import com.example.qlbds.user_service.dto.UserSummaryResponse;
import com.example.qlbds.user_service.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Người dùng", description = "Quản lý thông tin tài khoản người dùng")
public class UserController {

    private final UserService userService;

    // Xem thông tin cá nhân của người dùng đang đăng nhập
    @GetMapping("/me")
    @Operation(summary = "Xem thông tin cá nhân của tài khoản đang đăng nhập")
    public ResponseEntity<ApiResponse<UserProfileResponse>> me() throws ResourceNotFoundException {
        UserProfileResponse profile = userService.me();
        return ResponseEntity.ok(
                ApiResponse.success(profile, "Lấy thông tin cá nhân thành công"));
    }

    // Lấy danh sách tất cả người dùng (ADMIN)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách tất cả người dùng (chỉ ADMIN)")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getAllUsers() {
        List<UserSummaryResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(
                ApiResponse.success(users, "Lấy danh sách người dùng thành công"));
    }

    // Thay đổi vai trò của người dùng (ADMIN)
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thay đổi quyền của người dùng (chỉ ADMIN)")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> changeRole(
            @PathVariable Long id,
            @Valid @RequestBody ChangeUserRoleRequest request) throws ResourceNotFoundException {
        UserSummaryResponse updated = userService.changeRole(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(updated, "Thay đổi quyền thành công"));
    }
}
