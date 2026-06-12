package com.example.qlbds.user_service.dto;

import com.example.qlbds.shared.entity.enums.UserRole;

public record UserProfileResponse(
                Long id,
                String username,
                String email,
                String fullName,
                String phone,
                UserRole role,
                Boolean isActive,
                AgentInfo agent,
                OwnerInfo owner) {
        public record AgentInfo(
                        String licenseNumber,
                        String agencyName,
                        String slug) {
        }

        public record OwnerInfo(
                        String address,
                        String description) {
        }
}
