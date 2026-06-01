package com.example.qlbds.conversation_service.dto;

import com.example.qlbds.property_service.dto.PropertyResponse;
import com.example.qlbds.user_service.dto.UserSummaryResponse;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ConversationResponse {
    private Long id;
    private PropertyResponse property;
    private UserSummaryResponse otherParticipant;
    private List<UserSummaryResponse> participants;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Long unreadCount;
}
