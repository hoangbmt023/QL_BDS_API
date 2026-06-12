package com.example.qlbds.property_service.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Request tạo mới bất động sản.
 * - ownerId: bắt buộc (chủ nhà)
 * - agentId: tuỳ chọn (nếu AGENT đăng thay thì truyền vào)
 */
public record CreatePropertyRequest(

        @NotBlank(message = "Tiêu đề không được để trống")
        @Size(min = 10, max = 255, message = "Tiêu đề phải từ 10 đến 255 ký tự")
        String title,

        @Size(max = 5000, message = "Mô tả tối đa 5000 ký tự")
        String description,

        @NotNull(message = "Giá không được để trống")
        @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
        @Digits(integer = 13, fraction = 2, message = "Giá không hợp lệ (tối đa 13 số nguyên, 2 số thập phân)")
        BigDecimal price,

        @Positive(message = "Diện tích phải lớn hơn 0")
        Double area,

        @Min(value = 0, message = "Số phòng ngủ không được âm")
        @Max(value = 50, message = "Số phòng ngủ không hợp lệ")
        Integer bedrooms,

        @Min(value = 0, message = "Số phòng tắm không được âm")
        @Max(value = 50, message = "Số phòng tắm không hợp lệ")
        Integer bathrooms,

        @NotBlank(message = "Địa chỉ không được để trống")
        @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
        String address,

        @NotBlank(message = "Thành phố không được để trống")
        @Size(max = 100, message = "Thành phố tối đa 100 ký tự")
        String city,

        @NotBlank(message = "Quận/Huyện không được để trống")
        @Size(max = 100, message = "Quận/Huyện tối đa 100 ký tự")
        String district,

        @NotNull(message = "Chủ sở hữu (ownerId) không được để trống")
        Long ownerId,

        // agentId tuỳ chọn — truyền khi AGENT đăng thay cho OWNER
        Long agentId

) {}
