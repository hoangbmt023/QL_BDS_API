package com.example.qlbds.user_service.dto;

import jakarta.validation.constraints.Size;

public record BecomeOwnerRequest(

        @Size(max = 255, message = "Địa chỉ không vượt quá 255 ký tự")
        String address,

        String description
) {}
