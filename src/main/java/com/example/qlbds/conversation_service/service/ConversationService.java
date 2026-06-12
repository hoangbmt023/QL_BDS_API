package com.example.qlbds.conversation_service.service;

import com.example.qlbds.common.response.PageResponse;
import com.example.qlbds.conversation_service.dto.*;

public interface ConversationService {
    ConversationResponse getOrCreateConversation(ConversationCreateRequest request);
    PageResponse<ConversationResponse> getMyConversations(int page, int size);
    ConversationResponse getConversationById(Long id);
    MessageResponse sendMessage(Long conversationId, MessageRequest request);
    PageResponse<MessageResponse> getConversationMessages(Long conversationId, int page, int size);
    void markAsRead(Long conversationId);
    UnreadCountResponse getUnreadCount();

    MessageResponse editMessage(Long messageId, MessageRequest request);

    MessageResponse recallMessage(Long messageId);
}
