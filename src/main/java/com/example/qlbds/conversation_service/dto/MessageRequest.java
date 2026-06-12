package com.example.qlbds.conversation_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageRequest {
    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String content;
}
