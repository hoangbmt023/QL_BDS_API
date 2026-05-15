package com.example.qlbds.user_service.dto;

import com.example.qlbds.shared.entity.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public record AdminChangeRoleRequest(

                @NotNull(message = "Role không được để trống") UserRole role,

                // --- Dùng khi chuyển sang OWNER ---
                String address,
                String description,

                // --- Dùng khi chuyển sang AGENT ---
                String licenseNumber,
                String agencyName) {
}
