package com.example.qlbds.user_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank(message = "Họ tên không được để trống")
        String fullName,

        String phone,

        @Valid
        UpdateAgentInfo agent,

        @Valid
        UpdateOwnerInfo owner
) {
    public record UpdateAgentInfo(
            String licenseNumber,
            String agencyName
    ) {}

    public record UpdateOwnerInfo(
            String address,
            String description
    ) {}
}
