package com.example.qlbds.user_service.dto;

import com.example.qlbds.shared.entity.enums.UserRole;

public record UserSummaryResponse(
        Long id,
        String username,
        String email,
        String fullName,
        UserRole role,
        Boolean isActive
) {}
