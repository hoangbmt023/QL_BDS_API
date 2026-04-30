package com.example.qlbds.user_service.dto;

import java.time.LocalDateTime;

import com.example.qlbds.shared.entity.enums.AgentRequestStatus;

public record AgentRequestResponse(
        Long id,
        Long userId,
        String username,
        String agencyName,
        String licenseNumber,
        String note,
        AgentRequestStatus status,
        String adminNote,
        LocalDateTime createdAt
) {}
