package com.example.qlbds.user_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.qlbds.shared.dto.ApiResponse;
import com.example.qlbds.user_service.dto.*;
import com.example.qlbds.user_service.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Vai trò", description = "Nâng cấp vai trò người dùng")
public class RoleController {

    private final RoleService roleService;

    // Người dùng tự đăng ký thành Owner (chủ nhà / chủ bất động sản)
    @PostMapping("/become-owner")
    @Operation(summary = "Tự đăng ký thành Owner (chủ nhà / chủ bất động sản)")
    public ResponseEntity<ApiResponse<OwnerResponse>> becomeOwner(
            @RequestBody(required = false) BecomeOwnerRequest request) {

        // Cho phép body rỗng
        if (request == null) {
            request = new BecomeOwnerRequest(null, null);
        }

        OwnerResponse response = roleService.becomeOwner(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Đăng ký thành chủ nhà thành công"));
    }

    // Gui yêu cầu làm Agent (môi giới) đến Admin duyệt
    @PostMapping("/agent-requests")
    @Operation(summary = "Gửi yêu cầu trở thành môi giới (chờ Admin duyệt)")
    public ResponseEntity<ApiResponse<AgentRequestResponse>> submitAgentRequest(
            @Valid @RequestBody BecomeAgentRequest request) {

        AgentRequestResponse response = roleService.submitAgentRequest(request);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Yêu cầu làm môi giới đã được gửi, vui lòng chờ Admin xem xét"));
    }

    // Lấy danh sách yêu cầu làm Agent đang chờ duyệt (Admin)
    @GetMapping("/agent-requests")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy danh sách yêu cầu làm Agent đang chờ duyệt (Admin)")
    public ResponseEntity<ApiResponse<List<AgentRequestResponse>>> getPendingAgentRequests() {
        List<AgentRequestResponse> responses = roleService.getPendingAgentRequests();
        return ResponseEntity.ok(
                ApiResponse.success(responses, "Lấy danh sách yêu cầu thành công"));
    }

    // Admin duyệt hoặc từ chối yêu cầu làm Agent
    @PatchMapping("/agent-requests/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin duyệt hoặc từ chối yêu cầu làm Agent")
    public ResponseEntity<ApiResponse<AgentRequestResponse>> reviewAgentRequest(
            @PathVariable Long id,
            @Valid @RequestBody ReviewAgentRequest review) {

        AgentRequestResponse response = roleService.reviewAgentRequest(id, review);
        String msg = review.approved() ? "Đã duyệt yêu cầu, người dùng đã trở thành Môi giới"
                : "Đã từ chối yêu cầu";
        return ResponseEntity.ok(ApiResponse.success(response, msg));
    }
}
