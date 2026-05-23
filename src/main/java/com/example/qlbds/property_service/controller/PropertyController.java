package com.example.qlbds.property_service.controller;

import com.example.qlbds.common.response.PageResponse;
import com.example.qlbds.config.CurrentUserService;
import com.example.qlbds.property_service.dto.CreatePropertyRequest;
import com.example.qlbds.property_service.dto.PropertyAnalyticsResponse;
import com.example.qlbds.property_service.dto.PropertyResponse;
import com.example.qlbds.property_service.dto.UpdatePropertyRequest;
import com.example.qlbds.property_service.service.PropertyService;
import com.example.qlbds.shared.dto.ApiResponse;
import com.example.qlbds.shared.entity.enums.PropertyStatus;
import com.example.qlbds.user_service.entity.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
@Validated
@Tag(name = "Bất động sản", description = "Quản lý thông tin bất động sản")
public class PropertyController {

    private final PropertyService propertyService;
    private final CurrentUserService currentUserService;

    // Tạo mới bất động sản — OWNER hoặc AGENT
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'AGENT', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Tạo mới bất động sản")
    public ResponseEntity<ApiResponse<PropertyResponse>> create(
            @Valid @RequestBody CreatePropertyRequest request) {
        PropertyResponse response = propertyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tạo bất động sản thành công. Đang chờ duyệt."));
    }

    // Lấy danh sách bất động sản với phân trang và lọc (Public)
    @GetMapping
    @Operation(summary = "Lấy danh sách bất động sản (lọc động + phân trang)")
    public ResponseEntity<ApiResponse<PageResponse<PropertyResponse>>> findAll(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "district", required = false) String district,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "bedrooms", required = false) Integer bedrooms,
            @RequestParam(name = "bathrooms", required = false) Integer bathrooms,
            @RequestParam(name = "status", required = false) PropertyStatus status,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) int size) {

        int pageIndex = page > 0 ? page - 1 : 0;
        PageResponse<PropertyResponse> result = propertyService.findAll(
                search, city, district, minPrice, maxPrice, bedrooms, bathrooms, status, pageIndex, size, true, null);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách bất động sản thành công"));
    }

    // Lấy danh sách bất động sản của chính mình (Owner/Agent)
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('OWNER', 'AGENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lấy danh sách bất động sản của tôi (Owner/Agent)")
    public ResponseEntity<ApiResponse<PageResponse<PropertyResponse>>> findMyProperties(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "district", required = false) String district,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "bedrooms", required = false) Integer bedrooms,
            @RequestParam(name = "bathrooms", required = false) Integer bathrooms,
            @RequestParam(name = "status", required = false) PropertyStatus status,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) int size) {

        User currentUser = currentUserService.getCurrentUser();

        int pageIndex = page > 0 ? page - 1 : 0;
        PageResponse<PropertyResponse> result = propertyService.findAll(
                search, city, district, minPrice, maxPrice, bedrooms, bathrooms, status, pageIndex, size, false,
                currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách bất động sản của tôi thành công"));
    }

    // Lấy danh sách tất cả bất động sản cho Admin/Moderator (không quan tâm
    // visibility)
    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lấy tất cả danh sách bất động sản cho Admin/Moderator duyệt")
    public ResponseEntity<ApiResponse<PageResponse<PropertyResponse>>> findAllForAdmin(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "district", required = false) String district,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "bedrooms", required = false) Integer bedrooms,
            @RequestParam(name = "bathrooms", required = false) Integer bathrooms,
            @RequestParam(name = "status", required = false) PropertyStatus status,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) int size) {

        int pageIndex = page > 0 ? page - 1 : 0;
        PageResponse<PropertyResponse> result = propertyService.findAll(
                search, city, district, minPrice, maxPrice, bedrooms, bathrooms, status, pageIndex, size, false, null);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách bất động sản thành công"));
    }

    // Lấy chi tiết bất động sản theo Slug (Public)
    @GetMapping("/{slug}")
    @Operation(summary = "Lấy chi tiết bất động sản")
    public ResponseEntity<ApiResponse<PropertyResponse>> findBySlug(@PathVariable String slug) {
        PropertyResponse response = propertyService.findBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy chi tiết bất động sản thành công"));
    }

    // Cập nhật bất động sản — OWNER, AGENT hoặc ADMIN
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'AGENT', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cập nhật bất động sản (tự động yêu cầu duyệt lại)")
    public ResponseEntity<ApiResponse<PropertyResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePropertyRequest request) {
        PropertyResponse updated = propertyService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật thành công, chờ Moderator duyệt lại"));
    }

    // Xóa mềm bất động sản — OWNER, AGENT hoặc ADMIN
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'AGENT', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Xóa bất động sản (soft-delete)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        propertyService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa bất động sản thành công"));
    }

    // Upload nhiều ảnh cho một property — OWNER, AGENT hoặc ADMIN
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('OWNER', 'AGENT', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Upload nhiều hình ảnh cho bất động sản")
    public ResponseEntity<ApiResponse<List<String>>> uploadImages(
            @PathVariable Long id,
            @RequestPart("files") List<MultipartFile> files) {
        List<String> urls = propertyService.uploadImages(id, files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(urls, "Upload ảnh thành công"));
    }

    // Cập nhật 1 ảnh cụ thể
    @PostMapping(value = "/{id}/images/{imageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('OWNER', 'AGENT', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cập nhật thay thế một hình ảnh cụ thể của bất động sản")
    public ResponseEntity<ApiResponse<PropertyResponse.ImageInfo>> updateImage(
            @PathVariable Long id,
            @PathVariable Long imageId,
            @RequestPart("file") MultipartFile file) {
        PropertyResponse.ImageInfo imageInfo = propertyService.updateImage(id, imageId, file);
        return ResponseEntity.ok(ApiResponse.success(imageInfo, "Cập nhật ảnh thành công"));
    }

    // Xóa 1 ảnh cụ thể
    @DeleteMapping("/{id}/images/{imageId}")
    @PreAuthorize("hasAnyRole('OWNER', 'AGENT', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Xóa một hình ảnh cụ thể của bất động sản")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable Long id,
            @PathVariable Long imageId) {
        propertyService.deleteImage(id, imageId);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa ảnh thành công"));
    }

    // Gợi ý property tương tự (Public)
    @GetMapping("/{id}/similar")
    @Operation(summary = "Gợi ý bất động sản tương tự")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getSimilarProperties(
            @PathVariable Long id,
            @RequestParam(name = "limit", defaultValue = "5") @Min(1) int limit) {
        List<PropertyResponse> similar = propertyService.getSimilarProperties(id, limit);
        return ResponseEntity.ok(ApiResponse.success(similar, "Lấy danh sách gợi ý thành công"));
    }

    // Đổi trạng thái property — chỉ MODERATOR hoặc ADMIN
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cập nhật trạng thái (APPROVED, REJECTED, HIDDEN...) — Moderator/Admin")
    public ResponseEntity<ApiResponse<PropertyResponse>> changeStatus(
            @PathVariable Long id,
            @RequestParam("status") PropertyStatus status,
            @RequestParam(name = "reason", required = false) String reason) {
        PropertyResponse updated = propertyService.changeStatus(id, status, reason);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật trạng thái thành công"));
    }

    // Thống kê hiệu quả listing — ADMIN, MODERATOR, OWNER, AGENT
    @GetMapping("/{id}/analytics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'OWNER', 'AGENT')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Thống kê hiệu quả listing")
    public ResponseEntity<ApiResponse<PropertyAnalyticsResponse>> getAnalytics(@PathVariable Long id) {
        PropertyAnalyticsResponse analytics = propertyService.getAnalytics(id);
        return ResponseEntity.ok(ApiResponse.success(analytics, "Lấy thống kê thành công"));
    }
}
