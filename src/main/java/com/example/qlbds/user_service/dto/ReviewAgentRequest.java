package com.example.qlbds.user_service.dto;

import jakarta.validation.constraints.NotNull;

public record ReviewAgentRequest(

        @NotNull(message = "Trạng thái duyệt không được để trống")
        boolean approved,

        String adminNote
) {}
