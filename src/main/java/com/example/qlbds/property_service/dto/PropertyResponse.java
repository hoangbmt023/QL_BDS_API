package com.example.qlbds.property_service.dto;

import com.example.qlbds.shared.entity.enums.PropertyStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response trả về thông tin bất động sản.
 * - Chỉ bao gồm các trường cần hiển thị cho client.
 * - rejectionReason chỉ xuất khi không null (JsonInclude.NON_NULL).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropertyResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private Double area;
    private Integer bedrooms;
    private Integer bathrooms;
    private String address;
    private String city;
    private String district;
    private String slug;

    private PropertyStatus status;
    private Boolean visibility;
    private Integer viewCount;
    private Integer favoriteCount;

    // Chỉ hiển thị khi có lý do (bị từ chối hoặc ẩn bài)
    private String rejectionReason;

    private OwnerInfo owner;
    private AgentInfo agent;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Thông tin chủ sở hữu ──────────────────────────────────
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OwnerInfo {
        private Long id;
        private String fullName;
        private String phone;
        private String email;
    }

    // ── Thông tin môi giới ────────────────────────────────────
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AgentInfo {
        private Long id;
        private String fullName;
        private String email;
        private String agencyName;
        private String licenseNumber;
        private Double rating;
        private String slug;
    }
}
