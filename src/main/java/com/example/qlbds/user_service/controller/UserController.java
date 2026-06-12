package com.example.qlbds.user_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.qlbds.common.exception.ResourceNotFoundException;
import com.example.qlbds.shared.dto.ApiResponse;
import com.example.qlbds.user_service.dto.AdminChangeRoleRequest;
import com.example.qlbds.user_service.dto.UpdateProfileRequest;
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

    // Cập nhật thông tin cá nhân
    @PutMapping("/profile")
    @Operation(summary = "Cập nhật thông tin cá nhân (bao gồm cả thông tin Agent/Owner nếu có)")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) throws ResourceNotFoundException {
        UserProfileResponse updated = userService.updateProfile(request);
        return ResponseEntity.ok(
                ApiResponse.success(updated, "Cập nhật thông tin cá nhân thành công"));
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

    /**
     * Admin đổi role của người dùng — nghiệp vụ đầy đủ:
     *  - Lên OWNER : address, description (tuỳ chọn)
     *  - Lên AGENT : licenseNumber (bắt buộc), agencyName (tuỳ chọn)
     *  - Lên/xuống USER, ADMIN : không cần trường bổ sung
     *  - OWNER → AGENT : xóa mềm Owner, tạo/restore Agent
     *  - AGENT → OWNER : xóa mềm Agent, tạo/restore Owner
     */
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin đổi role người dùng (tự động xử lý Owner/Agent record)")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> changeRole(
            @PathVariable Long id,
            @Valid @RequestBody AdminChangeRoleRequest request) throws ResourceNotFoundException {
        UserSummaryResponse updated = userService.changeRole(id, request);
        return ResponseEntity.ok(
                ApiResponse.success(updated, "Thay đổi quyền thành công"));
    }

    // Xóa người dùng (ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa người dùng (soft-delete - chỉ ADMIN)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) throws ResourceNotFoundException {
        userService.deleteUser(id);
        return ResponseEntity.ok(
                ApiResponse.success("Xóa người dùng thành công"));
    }

    // Khôi phục người dùng (ADMIN)
    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Khôi phục người dùng đã bị xóa mềm (chỉ ADMIN)")
    public ResponseEntity<ApiResponse<Void>> restoreUser(@PathVariable Long id) throws ResourceNotFoundException {
        userService.restoreUser(id);
        return ResponseEntity.ok(
                ApiResponse.success("Khôi phục người dùng thành công"));
    }
}
