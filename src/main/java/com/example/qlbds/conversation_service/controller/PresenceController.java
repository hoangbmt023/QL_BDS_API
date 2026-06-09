package com.example.qlbds.conversation_service.controller;

import com.example.qlbds.conversation_service.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import com.example.qlbds.shared.dto.ApiResponse;

@RestController
@RequestMapping("/api/presence")
@RequiredArgsConstructor
@Tag(name = "Presence", description = "API quản lý trạng thái Online/Offline")
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "bearerAuth")
public class PresenceController {

    private final PresenceService presenceService;

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> getPresence(@PathVariable String username) {
        return ResponseEntity.ok(ApiResponse.success(Map.of("online", presenceService.isUserOnline(username)), "Lấy trạng thái online thành công"));
    }
}
