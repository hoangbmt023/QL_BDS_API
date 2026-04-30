package com.example.qlbds.user_service.dto;

import jakarta.validation.constraints.NotBlank;

public record BecomeAgentRequest(

        @NotBlank(message = "Số giấy phép hành nghề không được để trống")
        String licenseNumber,

        String agencyName,
        String note
) {}
