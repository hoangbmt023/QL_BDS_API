package com.example.qlbds.conversation_service.controller;

import com.example.qlbds.common.response.PageResponse;
import com.example.qlbds.shared.dto.ApiResponse;
import com.example.qlbds.conversation_service.dto.*;
import java.util.List;
import com.example.qlbds.conversation_service.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversation", description = "API quản lý tin nhắn và hội thoại")
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "bearerAuth")
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    @Operation(summary = "Tạo hoặc lấy hội thoại hiện có cho một bất động sản")
    public ResponseEntity<ApiResponse<ConversationResponse>> getOrCreateConversation(@Valid @RequestBody ConversationCreateRequest request) {
        ConversationResponse response = conversationService.getOrCreateConversation(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy hội thoại thành công"));
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy danh sách hội thoại của người dùng hiện tại")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getMyConversations(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        int pageIndex = page > 0 ? page - 1 : 0;
        PageResponse<ConversationResponse> pageResponse = conversationService.getMyConversations(pageIndex, size);
        return ResponseEntity.ok(ApiResponse.paginatedSuccess(pageResponse, "Lấy danh sách hội thoại thành công"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết hội thoại")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversationById(@PathVariable Long id) {
        ConversationResponse response = conversationService.getConversationById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy chi tiết hội thoại thành công"));
    }

    @PostMapping("/{id}/messages")
    @Operation(summary = "Gửi tin nhắn vào hội thoại")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable Long id,
            @Valid @RequestBody MessageRequest request) {
        MessageResponse response = conversationService.sendMessage(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Gửi tin nhắn thành công"));
    }

    @GetMapping("/{id}/messages")
    @Operation(summary = "Lấy danh sách tin nhắn trong hội thoại")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getConversationMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size) {
        int pageIndex = page > 0 ? page - 1 : 0;
        PageResponse<MessageResponse> pageResponse = conversationService.getConversationMessages(id, pageIndex, size);
        return ResponseEntity.ok(ApiResponse.paginatedSuccess(pageResponse, "Lấy danh sách tin nhắn thành công"));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Đánh dấu tất cả tin nhắn trong hội thoại là đã đọc")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        conversationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Đánh dấu đã đọc thành công"));
    }

    @PatchMapping("/messages/{messageId}/edit")
    @Operation(summary = "Sửa nội dung tin nhắn")
    public ResponseEntity<ApiResponse<MessageResponse>> editMessage(
            @PathVariable Long messageId,
            @Valid @RequestBody MessageRequest request) {
        MessageResponse response = conversationService.editMessage(messageId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Sửa tin nhắn thành công"));
    }

    @PostMapping("/messages/{messageId}/recall")
    @Operation(summary = "Thu hồi tin nhắn")
    public ResponseEntity<ApiResponse<MessageResponse>> recallMessage(@PathVariable Long messageId) {
        MessageResponse response = conversationService.recallMessage(messageId);
        return ResponseEntity.ok(ApiResponse.success(response, "Thu hồi tin nhắn thành công"));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Lấy tổng số tin nhắn chưa đọc của người dùng")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount() {
        UnreadCountResponse response = conversationService.getUnreadCount();
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy số lượng tin chưa đọc thành công"));
    }
}
