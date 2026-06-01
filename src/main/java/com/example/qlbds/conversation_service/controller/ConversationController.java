package com.example.qlbds.conversation_service.controller;

import com.example.qlbds.common.response.PageResponse;
import com.example.qlbds.conversation_service.dto.*;
import com.example.qlbds.conversation_service.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversation", description = "API quản lý tin nhắn và hội thoại")
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    @Operation(summary = "Tạo hoặc lấy hội thoại hiện có cho một bất động sản")
    public ResponseEntity<ConversationResponse> getOrCreateConversation(@Valid @RequestBody ConversationCreateRequest request) {
        return ResponseEntity.ok(conversationService.getOrCreateConversation(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy danh sách hội thoại của người dùng hiện tại")
    public ResponseEntity<PageResponse<ConversationResponse>> getMyConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(conversationService.getMyConversations(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết hội thoại")
    public ResponseEntity<ConversationResponse> getConversationById(@PathVariable Long id) {
        return ResponseEntity.ok(conversationService.getConversationById(id));
    }

    @PostMapping("/{id}/messages")
    @Operation(summary = "Gửi tin nhắn vào hội thoại")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable Long id,
            @Valid @RequestBody MessageRequest request) {
        return ResponseEntity.ok(conversationService.sendMessage(id, request));
    }

    @GetMapping("/{id}/messages")
    @Operation(summary = "Lấy danh sách tin nhắn trong hội thoại")
    public ResponseEntity<PageResponse<MessageResponse>> getConversationMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(conversationService.getConversationMessages(id, page, size));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Đánh dấu tất cả tin nhắn trong hội thoại là đã đọc")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        conversationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Lấy tổng số tin nhắn chưa đọc của người dùng")
    public ResponseEntity<UnreadCountResponse> getUnreadCount() {
        return ResponseEntity.ok(conversationService.getUnreadCount());
    }
}
