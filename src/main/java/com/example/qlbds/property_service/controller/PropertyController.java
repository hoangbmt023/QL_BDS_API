package com.example.qlbds.property_service.controller;

import com.example.qlbds.common.response.PageResponse;
import com.example.qlbds.property_service.dto.CreatePropertyRequest;
import com.example.qlbds.property_service.dto.PropertyResponse;
import com.example.qlbds.property_service.dto.UpdatePropertyRequest;
import com.example.qlbds.property_service.service.PropertyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
@Validated
@Tag(name = "Bất động sản", description = "Quản lý thông tin bất động sản")
public class PropertyController {

    private final PropertyService propertyService;

    // Tạo mới một bất động sản
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo mới bất động sản", description = "Tạo mới một bất động sản với thông tin chi tiết")
    public PropertyResponse create(@Valid @RequestBody CreatePropertyRequest request) {
        return propertyService.create(request);
    }

    // Lấy danh sách bất động sản với phân trang và tìm kiếm
    @GetMapping
    @Operation(summary = "Lấy danh sách bất động sản", description = "Lấy danh sách bất động sản với phân trang và tìm kiếm theo tên hoặc địa chỉ")
    public PageResponse<PropertyResponse> findAll(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1") int size) {

        return propertyService.findAll(search, page, size);
    }

    // Lấy chi tiết bất động sản theo ID
    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết bất động sản", description = "Lấy chi tiết thông tin bất động sản theo ID")
    public PropertyResponse findById(@PathVariable(name = "id") Long id) {
        return propertyService.findById(id);
    }

    // Cập nhật thông tin bất động sản theo ID
    @PatchMapping("/{id}")
    @Operation(summary = "Cập nhật bất động sản", description = "Cập nhật thông tin bất động sản theo ID")
    public PropertyResponse update(
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody UpdatePropertyRequest request) {
        return propertyService.update(id, request);
    }

    // Xóa bất động sản theo ID (ẩn visibility)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Xóa bất động sản", description = "Xóa bất động sản theo ID (thực tế là ẩn visibility)")
    public void delete(@PathVariable(name = "id") Long id) {
        propertyService.delete(id);
    }
}
