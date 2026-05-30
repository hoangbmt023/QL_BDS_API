package com.example.qlbds.favorite_service.controller;

import com.example.qlbds.common.response.PageResponse;
import com.example.qlbds.favorite_service.service.FavoriteService;
import com.example.qlbds.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.qlbds.favorite_service.dto.DeleteMultipleFavoritesRequest;
import com.example.qlbds.favorite_service.dto.FavoriteActionResponse;
import com.example.qlbds.favorite_service.dto.FavoriteRequest;
import com.example.qlbds.favorite_service.dto.FavoriteResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Validated
@Tag(name = "Yêu thích", description = "Quản lý danh sách bất động sản yêu thích")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Thêm bất động sản vào danh sách yêu thích")
    public ResponseEntity<ApiResponse<FavoriteActionResponse>> addFavorite(
            @Valid @RequestBody FavoriteRequest request) {
        FavoriteActionResponse response = favoriteService.addFavorite(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Thêm vào danh sách yêu thích thành công"));
    }

    @DeleteMapping("/{propertyId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Xóa bất động sản khỏi danh sách yêu thích")
    public ResponseEntity<ApiResponse<FavoriteActionResponse>> removeFavorite(@PathVariable Long propertyId) {
        FavoriteActionResponse response = favoriteService.removeFavorite(propertyId);
        return ResponseEntity.ok(ApiResponse.success(response, "Xóa khỏi danh sách yêu thích thành công"));
    }

    @DeleteMapping("/multiple")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Xóa nhiều bất động sản khỏi danh sách yêu thích")
    public ResponseEntity<ApiResponse<Void>> removeMultipleFavorites(
            @Valid @RequestBody DeleteMultipleFavoritesRequest request) {
        favoriteService.removeMultipleFavorites(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa danh sách yêu thích thành công"));
    }

    @DeleteMapping("/all")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Xóa tất cả bất động sản khỏi danh sách yêu thích")
    public ResponseEntity<ApiResponse<Void>> removeAllFavorites() {
        favoriteService.removeAllFavorites();
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa toàn bộ danh sách yêu thích"));
    }

    @GetMapping("/{propertyId}/check")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Kiểm tra xem bất động sản đã được yêu thích chưa")
    public ResponseEntity<ApiResponse<FavoriteActionResponse>> checkFavorite(@PathVariable Long propertyId) {
        FavoriteActionResponse response = favoriteService.checkFavorite(propertyId);
        return ResponseEntity.ok(ApiResponse.success(response, "Kiểm tra thành công"));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lấy danh sách bất động sản yêu thích của tôi")
    public ResponseEntity<ApiResponse<PageResponse<FavoriteResponse>>> getMyFavorites(
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) int size) {

        int pageIndex = page > 0 ? page - 1 : 0;
        PageResponse<FavoriteResponse> response = favoriteService.getMyFavorites(pageIndex, size);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy danh sách yêu thích thành công"));
    }
}
