package com.example.qlbds.user_service.dto;

import com.example.qlbds.shared.entity.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public record ChangeUserRoleRequest(
        @NotNull(message = "Role is required")
        UserRole role
) {}
