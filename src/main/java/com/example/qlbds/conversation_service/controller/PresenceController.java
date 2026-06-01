package com.example.qlbds.conversation_service.controller;

import com.example.qlbds.conversation_service.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/presence")
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;

    @GetMapping("/{username}")
    public ResponseEntity<Map<String, Boolean>> getPresence(@PathVariable String username) {
        return ResponseEntity.ok(Map.of("online", presenceService.isUserOnline(username)));
    }
}
