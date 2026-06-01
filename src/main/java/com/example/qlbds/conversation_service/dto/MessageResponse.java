package com.example.qlbds.conversation_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
