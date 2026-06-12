package com.example.qlbds.conversation_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConversationCreateRequest {
    @NotNull(message = "Mã bất động sản không được để trống")
    private Long propertyId;
}
