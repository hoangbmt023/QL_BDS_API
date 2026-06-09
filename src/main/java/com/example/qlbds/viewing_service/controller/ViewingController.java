package com.example.qlbds.viewing_service.controller;

import com.example.qlbds.common.response.PageResponse;
import com.example.qlbds.shared.dto.ApiResponse;
import com.example.qlbds.viewing_service.dto.ViewingCreateRequest;
import com.example.qlbds.viewing_service.dto.ViewingResponse;
import com.example.qlbds.viewing_service.dto.ViewingStatusRequest;
import com.example.qlbds.viewing_service.dto.ViewingRescheduleRequest;
import com.example.qlbds.viewing_service.dto.ViewingStatsResponse;
import com.example.qlbds.viewing_service.dto.AvailableSlotResponse;
import com.example.qlbds.shared.entity.enums.ViewingStatus;
import com.example.qlbds.viewing_service.service.ViewingService;

import java.time.LocalDate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/viewings")
@RequiredArgsConstructor
@Validated
@Tag(name = "Lịch xem nhà", description = "Quản lý đặt lịch xem bất động sản")
public class ViewingController {

    private final ViewingService viewingService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Đặt lịch xem nhà mới")
    public ResponseEntity<ApiResponse<ViewingResponse>> createViewing(
            @Valid @RequestBody ViewingCreateRequest request) {
        ViewingResponse response = viewingService.createViewing(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Đặt lịch xem nhà thành công"));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lấy danh sách lịch xem của người dùng hiện tại")
    public ResponseEntity<ApiResponse<PageResponse<ViewingResponse>>> getMyViewings(
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) int size,
            @RequestParam(required = false) ViewingStatus status,
            @RequestParam(defaultValue = "false") boolean upcoming,
            @RequestParam(required = false, defaultValue = "scheduledTime,desc") String sort) {
        
        int pageIndex = page > 0 ? page - 1 : 0;
        PageResponse<ViewingResponse> response = viewingService.getMyViewings(pageIndex, size, status, upcoming, sort);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách lịch xem thành công"));
    }

    @GetMapping("/managed")
    @PreAuthorize("hasAnyRole('OWNER', 'AGENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lấy danh sách lịch hẹn của các bất động sản do mình quản lý (Agent/Owner)")
    public ResponseEntity<ApiResponse<PageResponse<ViewingResponse>>> getManagedViewings(
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) int size,
            @RequestParam(required = false) ViewingStatus status,
            @RequestParam(defaultValue = "false") boolean upcoming,
            @RequestParam(required = false, defaultValue = "scheduledTime,asc") String sort) {
        
        int pageIndex = page > 0 ? page - 1 : 0;
        PageResponse<ViewingResponse> response = viewingService.getManagedViewings(pageIndex, size, status, upcoming, sort);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách lịch hẹn cần quản lý thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Xem chi tiết một lịch hẹn")
    public ResponseEntity<ApiResponse<ViewingResponse>> getViewingById(@PathVariable Long id) {
        ViewingResponse response = viewingService.getViewingById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy chi tiết lịch hẹn thành công"));
    }

    @PatchMapping("/{id}/reschedule")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Đổi giờ lịch xem nhà (Chỉ dành cho người đặt)")
    public ResponseEntity<ApiResponse<ViewingResponse>> rescheduleViewing(
            @PathVariable Long id,
            @Valid @RequestBody ViewingRescheduleRequest request) {
        ViewingResponse response = viewingService.rescheduleViewing(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Đổi giờ xem nhà thành công"));
    }

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lấy thống kê lịch xem nhà")
    public ResponseEntity<ApiResponse<ViewingStatsResponse>> getViewingStats(
            @RequestParam(name = "asManager", defaultValue = "false") boolean asManager) {
        ViewingStatsResponse response = viewingService.getViewingStats(asManager);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy thống kê thành công"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cập nhật trạng thái lịch xem")
    public ResponseEntity<ApiResponse<ViewingResponse>> updateViewingStatus(
            @PathVariable Long id,
            @Valid @RequestBody ViewingStatusRequest request) {
        ViewingResponse response = viewingService.updateViewingStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cập nhật trạng thái lịch xem thành công"));
    }

    @GetMapping("/properties/{propertyId}/available-slots")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lấy danh sách các khung giờ còn trống của một bất động sản trong ngày")
    public ResponseEntity<ApiResponse<AvailableSlotResponse>> getAvailableSlots(
            @PathVariable Long propertyId,
            @RequestParam(required = false) LocalDate date) {
        AvailableSlotResponse response = viewingService.getAvailableSlots(propertyId, date);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách khung giờ trống thành công"));
    }
}
