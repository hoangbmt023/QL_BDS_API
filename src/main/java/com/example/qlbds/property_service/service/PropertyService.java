package com.example.qlbds.property_service.service;

import com.example.qlbds.common.response.PageResponse;
import com.example.qlbds.property_service.dto.CreatePropertyRequest;
import com.example.qlbds.property_service.dto.PropertyAnalyticsResponse;
import com.example.qlbds.property_service.dto.PropertyResponse;
import com.example.qlbds.property_service.dto.UpdatePropertyRequest;
import com.example.qlbds.property_service.entity.Property;
import com.example.qlbds.shared.entity.enums.PropertyStatus;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface PropertyService {

    // Tạo mới bất động sản (OWNER hoặc AGENT).
    PropertyResponse create(CreatePropertyRequest request);

    // Lấy danh sách bất động sản với phân trang, tìm kiếm và lọc động.
    PageResponse<PropertyResponse> findAll(
            String search, String city, String district,
            BigDecimal minPrice, BigDecimal maxPrice,
            Integer bedrooms, Integer bathrooms,
            PropertyStatus status, int page, int size, boolean onlyVisible, Long userId);

    // Lấy chi tiết một bất động sản theo ID (chỉ visible & chưa xóa).
    PropertyResponse findById(Long id);

    // Lấy chi tiết một bất động sản theo Slug (chỉ visible & chưa xóa).
    PropertyResponse findBySlug(String slug);

    // Cập nhật thông tin bất động sản — status tự động reset về PENDING.
    PropertyResponse update(Long id, UpdatePropertyRequest request);

    // Xóa mềm bất động sản.
    void delete(Long id);

    // Tìm thực thể Property (dùng nội bộ giữa các service).
    Property findPropertyById(Long id);

    // Upload nhiều ảnh cho một property, trả về danh sách URL.
    List<String> uploadImages(Long propertyId, List<MultipartFile> files);

    // Cập nhật một ảnh cụ thể.
    PropertyResponse.ImageInfo updateImage(Long propertyId, Long imageId, MultipartFile file);

    // Xóa một ảnh cụ thể.
    void deleteImage(Long propertyId, Long imageId);

    // Lấy danh sách property tương tự (cùng khu vực).
    List<PropertyResponse> getSimilarProperties(Long id, int limit);

    // Moderator/Admin đổi trạng thái (APPROVED, REJECTED, HIDDEN...).
    PropertyResponse changeStatus(Long id, PropertyStatus newStatus, String reason);

    // Xem thống kê hiệu quả listing.
    PropertyAnalyticsResponse getAnalytics(Long id);
}
