package com.example.qlbds.property_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.qlbds.shared.entity.enums.PropertyStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    private OwnerInfo owner;
    private AgentInfo agent;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerInfo {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String phone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgentInfo {
        private Long id;
        private String username;
        private String email;
        private String agencyName;
        private String licenseNumber;
        private Double rating;
        private String slug;
    }
}
