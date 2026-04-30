package com.example.qlbds.user_service.dto;

public record OwnerResponse(
        Long id,
        Long userId,
        String username,
        String fullName,
        String address,
        String description
) {}
