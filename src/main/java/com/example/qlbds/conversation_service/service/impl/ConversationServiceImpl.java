package com.example.qlbds.conversation_service.service.impl;

import com.example.qlbds.common.exception.InvalidResourceException;
import com.example.qlbds.common.exception.ResourceNotFoundException;
import com.example.qlbds.common.response.PageResponse;
import com.example.qlbds.config.CurrentUserService;
import com.example.qlbds.conversation_service.dto.*;
import com.example.qlbds.conversation_service.entity.Conversation;
import com.example.qlbds.conversation_service.entity.Message;
import com.example.qlbds.conversation_service.mapper.ConversationMapper;
import com.example.qlbds.conversation_service.mapper.MessageMapper;
import com.example.qlbds.conversation_service.repository.ConversationRepository;
import com.example.qlbds.conversation_service.repository.MessageRepository;
import com.example.qlbds.conversation_service.service.ConversationService;
import com.example.qlbds.property_service.entity.Property;
import com.example.qlbds.property_service.repository.PropertyRepository;
import com.example.qlbds.user_service.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final PropertyRepository propertyRepository;
    private final CurrentUserService currentUserService;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public ConversationResponse getOrCreateConversation(ConversationCreateRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        Property property = propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Bất động sản không tồn tại."));

        User ownerOrAgent = property.getAgent() != null ? property.getAgent().getUser() : property.getOwner().getUser();
        if (ownerOrAgent == null || ownerOrAgent.getId().equals(currentUser.getId())) {
            throw new InvalidResourceException("Không thể tạo cuộc hội thoại với chính mình.");
        }

        Optional<Conversation> existingOpt = conversationRepository.findByPropertyAndParticipants(property.getId(), currentUser, ownerOrAgent);
        Conversation conversation;
        if (existingOpt.isPresent()) {
            conversation = existingOpt.get();
        } else {
            conversation = Conversation.builder()
                    .property(property)
                    .userOne(currentUser)
                    .userTwo(ownerOrAgent)
                    .lastMessageAt(LocalDateTime.now())
                    .build();
            conversation = conversationRepository.save(conversation);
        }

        return mapToResponse(conversation, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ConversationResponse> getMyConversations(int page, int size) {
        User currentUser = currentUserService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<Conversation> conversations = conversationRepository.findByUserOrderByLastMessageAtDesc(currentUser, pageable);

        List<ConversationResponse> data = conversations.stream()
                .map(conv -> mapToResponse(conv, currentUser))
                .collect(Collectors.toList());

        return PageResponse.<ConversationResponse>builder()
                .data(data)
                .currentPage(conversations.getNumber() + 1)
                .pageSize(conversations.getSize())
                .totalElements(conversations.getTotalElements())
                .totalPages(conversations.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponse getConversationById(Long id) {
        User currentUser = currentUserService.getCurrentUser();
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hội thoại."));

        if (!conversation.getUserOne().getId().equals(currentUser.getId()) &&
                !conversation.getUserTwo().getId().equals(currentUser.getId())) {
            throw new InvalidResourceException("Bạn không có quyền truy cập cuộc hội thoại này.");
        }

        return mapToResponse(conversation, currentUser);
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(Long conversationId, MessageRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hội thoại."));

        if (!conversation.getUserOne().getId().equals(currentUser.getId()) &&
                !conversation.getUserTwo().getId().equals(currentUser.getId())) {
            throw new InvalidResourceException("Bạn không có quyền gửi tin nhắn vào cuộc hội thoại này.");
        }

        Message message = Message.builder()
                .conversation(conversation)
                .sender(currentUser)
                .content(request.getContent())
                .isRead(false)
                .build();
        message = messageRepository.save(message);

        conversation.setLastMessageId(message.getId());
        conversation.setLastMessageAt(message.getCreatedAt());
        conversationRepository.save(conversation);

        MessageResponse response = messageMapper.toResponse(message);

        // Send via WebSocket
        User recipient = conversation.getUserOne().getId().equals(currentUser.getId()) ? conversation.getUserTwo() : conversation.getUserOne();
        messagingTemplate.convertAndSendToUser(
                recipient.getUsername(),
                "/queue/messages",
                response
        );

        // Push realtime unread count update to recipient
        Long recipientUnreadCount = messageRepository.countTotalUnreadMessagesForUser(recipient);
        messagingTemplate.convertAndSendToUser(
                recipient.getUsername(),
                "/queue/unread-count",
                new UnreadCountResponse(recipientUnreadCount != null ? recipientUnreadCount : 0L)
        );

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MessageResponse> getConversationMessages(Long conversationId, int page, int size) {
        User currentUser = currentUserService.getCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hội thoại."));

        if (!conversation.getUserOne().getId().equals(currentUser.getId()) &&
                !conversation.getUserTwo().getId().equals(currentUser.getId())) {
            throw new InvalidResourceException("Bạn không có quyền truy cập cuộc hội thoại này.");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findByConversationOrderByCreatedAtDesc(conversation, pageable);

        List<MessageResponse> data = messages.stream()
                .map(messageMapper::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<MessageResponse>builder()
                .data(data)
                .currentPage(messages.getNumber() + 1)
                .pageSize(messages.getSize())
                .totalElements(messages.getTotalElements())
                .totalPages(messages.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public void markAsRead(Long conversationId) {
        User currentUser = currentUserService.getCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc hội thoại."));

        if (!conversation.getUserOne().getId().equals(currentUser.getId()) &&
                !conversation.getUserTwo().getId().equals(currentUser.getId())) {
            throw new InvalidResourceException("Bạn không có quyền truy cập cuộc hội thoại này.");
        }

        messageRepository.markMessagesAsRead(conversation, currentUser);

        // Push read receipt
        User recipient = conversation.getUserOne().getId().equals(currentUser.getId()) ? conversation.getUserTwo() : conversation.getUserOne();
        messagingTemplate.convertAndSendToUser(
                recipient.getUsername(),
                "/queue/messages/read",
                conversationId
        );

        // Push realtime unread count update to the current user (who just marked messages as read)
        Long currentUserUnreadCount = messageRepository.countTotalUnreadMessagesForUser(currentUser);
        messagingTemplate.convertAndSendToUser(
                currentUser.getUsername(),
                "/queue/unread-count",
                new UnreadCountResponse(currentUserUnreadCount != null ? currentUserUnreadCount : 0L)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount() {
        User currentUser = currentUserService.getCurrentUser();
        Long unreadCount = messageRepository.countTotalUnreadMessagesForUser(currentUser);
        return new UnreadCountResponse(unreadCount != null ? unreadCount : 0L);
    }

    @Override
    @Transactional
    public MessageResponse editMessage(Long messageId, MessageRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin nhắn."));

        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new InvalidResourceException("Bạn chỉ có thể sửa tin nhắn của chính mình.");
        }

        if (message.getIsRecalled() != null && message.getIsRecalled()) {
            throw new InvalidResourceException("Không thể sửa tin nhắn đã thu hồi.");
        }

        message.setContent(request.getContent());
        message.setIsEdited(true);
        message = messageRepository.save(message);

        MessageResponse response = messageMapper.toResponse(message);

        // Push update to recipient
        Conversation conversation = message.getConversation();
        User recipient = conversation.getUserOne().getId().equals(currentUser.getId()) ? conversation.getUserTwo() : conversation.getUserOne();
        messagingTemplate.convertAndSendToUser(
                recipient.getUsername(),
                "/queue/messages/update",
                response
        );

        return response;
    }

    @Override
    @Transactional
    public MessageResponse recallMessage(Long messageId) {
        User currentUser = currentUserService.getCurrentUser();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tin nhắn."));

        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new InvalidResourceException("Bạn chỉ có thể thu hồi tin nhắn của chính mình.");
        }

        if (message.getIsRecalled() != null && message.getIsRecalled()) {
            throw new InvalidResourceException("Tin nhắn này đã được thu hồi.");
        }

        message.setContent("Tin nhắn đã bị thu hồi");
        message.setIsRecalled(true);
        message = messageRepository.save(message);

        MessageResponse response = messageMapper.toResponse(message);

        // Push update to recipient
        Conversation conversation = message.getConversation();
        User recipient = conversation.getUserOne().getId().equals(currentUser.getId()) ? conversation.getUserTwo() : conversation.getUserOne();
        messagingTemplate.convertAndSendToUser(
                recipient.getUsername(),
                "/queue/messages/update",
                response
        );

        return response;
    }

    private ConversationResponse mapToResponse(Conversation conversation, User currentUser) {
        Long unreadCount = messageRepository.countUnreadMessagesInConversation(conversation, currentUser);
        String lastMessageContent = null;
        Long lastMessageSenderId = null;
        if (conversation.getLastMessageId() != null) {
            Message lastMsg = messageRepository.findById(conversation.getLastMessageId()).orElse(null);
            if (lastMsg != null) {
                lastMessageContent = lastMsg.getContent();
                lastMessageSenderId = lastMsg.getSender().getId();
            }
        }
        return conversationMapper.toResponse(conversation, currentUser, unreadCount, lastMessageContent, lastMessageSenderId);
    }
}
