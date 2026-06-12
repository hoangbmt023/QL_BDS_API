package com.example.qlbds.conversation_service.service;

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
import com.example.qlbds.property_service.entity.Property;
import com.example.qlbds.property_service.repository.PropertyRepository;
import com.example.qlbds.user_service.entity.Agent;
import com.example.qlbds.user_service.entity.Owner;
import com.example.qlbds.user_service.entity.User;
import com.example.qlbds.conversation_service.service.impl.ConversationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConversationServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ConversationServiceImpl conversationService;

    private User currentUser;
    private User otherUser;
    private Property property;
    private Conversation conversation;
    private Message message;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("currentuser");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");

        property = new Property();
        property.setId(10L);

        Owner owner = new Owner();
        owner.setId(2L);
        owner.setUser(otherUser);
        property.setOwner(owner);

        conversation = new Conversation();
        conversation.setId(100L);
        conversation.setProperty(property);
        conversation.setUserOne(currentUser);
        conversation.setUserTwo(otherUser);

        message = new Message();
        message.setId(1000L);
        message.setConversation(conversation);
        message.setSender(currentUser);
        message.setContent("Hello");
        message.setIsRead(false);
        message.setIsRecalled(false);
        message.setIsEdited(false);
    }

    // ==========================================
    // getOrCreateConversation()
    // ==========================================

    @Test
    @DisplayName("getOrCreateConversation_CreateNewConversation_Success")
    void getOrCreateConversation_CreateNewConversation_Success() {
        ConversationCreateRequest request = new ConversationCreateRequest();
        request.setPropertyId(10L);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(10L)).thenReturn(Optional.of(property));
        when(conversationRepository.findByPropertyAndParticipants(10L, currentUser, otherUser))
                .thenReturn(Optional.empty());
        when(conversationRepository.save(any(Conversation.class))).thenReturn(conversation);

        ConversationResponse responseDto = new ConversationResponse();
        when(conversationMapper.toResponse(eq(conversation), eq(currentUser), any(), any(), any()))
                .thenReturn(responseDto);

        ConversationResponse result = conversationService.getOrCreateConversation(request);

        assertNotNull(result);
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    @DisplayName("getOrCreateConversation_ReturnExistingConversation_Success")
    void getOrCreateConversation_ReturnExistingConversation_Success() {
        ConversationCreateRequest request = new ConversationCreateRequest();
        request.setPropertyId(10L);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(10L)).thenReturn(Optional.of(property));
        when(conversationRepository.findByPropertyAndParticipants(10L, currentUser, otherUser))
                .thenReturn(Optional.of(conversation));

        ConversationResponse responseDto = new ConversationResponse();
        when(conversationMapper.toResponse(eq(conversation), eq(currentUser), any(), any(), any()))
                .thenReturn(responseDto);

        ConversationResponse result = conversationService.getOrCreateConversation(request);

        assertNotNull(result);
        verify(conversationRepository, never()).save(any(Conversation.class));
    }

    @Test
    @DisplayName("getOrCreateConversation_PropertyNotFound")
    void getOrCreateConversation_PropertyNotFound() {
        ConversationCreateRequest request = new ConversationCreateRequest();
        request.setPropertyId(10L);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> conversationService.getOrCreateConversation(request));
    }

    @Test
    @DisplayName("getOrCreateConversation_SelfConversation_AsOwner")
    void getOrCreateConversation_SelfConversation_AsOwner() {
        ConversationCreateRequest request = new ConversationCreateRequest();
        request.setPropertyId(10L);

        // currentUser is owner
        Owner owner = new Owner();
        owner.setId(1L);
        owner.setUser(currentUser);
        property.setOwner(owner);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(10L)).thenReturn(Optional.of(property));

        assertThrows(InvalidResourceException.class, () -> conversationService.getOrCreateConversation(request));
    }

    @Test
    @DisplayName("getOrCreateConversation_SelfConversation_AsAgent")
    void getOrCreateConversation_SelfConversation_AsAgent() {
        ConversationCreateRequest request = new ConversationCreateRequest();
        request.setPropertyId(10L);

        // currentUser is agent
        Agent agent = new Agent();
        agent.setId(1L);
        agent.setUser(currentUser);
        property.setAgent(agent);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(10L)).thenReturn(Optional.of(property));

        assertThrows(InvalidResourceException.class, () -> conversationService.getOrCreateConversation(request));
    }

    // ==========================================
    // getMyConversations()
    // ==========================================

    @Test
    @DisplayName("getMyConversations_Success")
    void getMyConversations_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        Page<Conversation> page = new PageImpl<>(List.of(conversation));
        when(conversationRepository.findByUserOrderByLastMessageAtDesc(eq(currentUser), any(Pageable.class)))
                .thenReturn(page);

        when(conversationMapper.toResponse(eq(conversation), eq(currentUser), any(), any(), any()))
                .thenReturn(new ConversationResponse());

        PageResponse<ConversationResponse> result = conversationService.getMyConversations(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
    }

    @Test
    @DisplayName("getMyConversations_Empty")
    void getMyConversations_Empty() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        Page<Conversation> page = new PageImpl<>(Collections.emptyList());
        when(conversationRepository.findByUserOrderByLastMessageAtDesc(eq(currentUser), any(Pageable.class)))
                .thenReturn(page);

        PageResponse<ConversationResponse> result = conversationService.getMyConversations(0, 10);

        assertNotNull(result);
        assertEquals(0, result.getData().size());
    }

    // ==========================================
    // getConversationById()
    // ==========================================

    @Test
    @DisplayName("getConversationById_UserOne_Success")
    void getConversationById_UserOne_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));
        when(conversationMapper.toResponse(eq(conversation), eq(currentUser), any(), any(), any()))
                .thenReturn(new ConversationResponse());

        ConversationResponse result = conversationService.getConversationById(100L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("getConversationById_UserTwo_Success")
    void getConversationById_UserTwo_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(otherUser);
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));
        when(conversationMapper.toResponse(eq(conversation), eq(otherUser), any(), any(), any()))
                .thenReturn(new ConversationResponse());

        ConversationResponse result = conversationService.getConversationById(100L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("getConversationById_NotFound")
    void getConversationById_NotFound() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(conversationRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> conversationService.getConversationById(100L));
    }

    @Test
    @DisplayName("getConversationById_NoPermission")
    void getConversationById_NoPermission() {
        User stranger = new User();
        stranger.setId(99L);
        when(currentUserService.getCurrentUser()).thenReturn(stranger);
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));

        assertThrows(InvalidResourceException.class, () -> conversationService.getConversationById(100L));
    }

    // ==========================================
    // sendMessage()
    // ==========================================

    @Test
    @DisplayName("sendMessage_Success")
    void sendMessage_Success() {
        MessageRequest request = new MessageRequest();
        request.setContent("New message");

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));

        when(messageRepository.save(any(Message.class))).thenAnswer(i -> {
            Message m = i.getArgument(0);
            m.setId(1001L);
            return m;
        });

        when(messageRepository.countTotalUnreadMessagesForUser(otherUser)).thenReturn(5L);
        when(messageMapper.toResponse(any(Message.class))).thenReturn(new MessageResponse());

        MessageResponse result = conversationService.sendMessage(100L, request);

        assertNotNull(result);
        verify(messageRepository).save(any(Message.class));
        verify(conversationRepository).save(conversation);
        verify(messagingTemplate).convertAndSendToUser(eq(otherUser.getUsername()), eq("/queue/messages"),
                any(MessageResponse.class));
        verify(messagingTemplate).convertAndSendToUser(eq(otherUser.getUsername()), eq("/queue/unread-count"),
                any(UnreadCountResponse.class));
    }

    @Test
    @DisplayName("sendMessage_Success_UnreadCountNull")
    void sendMessage_Success_UnreadCountNull() {
        MessageRequest request = new MessageRequest();
        request.setContent("New message");

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));

        when(messageRepository.save(any(Message.class))).thenAnswer(i -> {
            Message m = i.getArgument(0);
            m.setId(1001L);
            return m;
        });

        when(messageRepository.countTotalUnreadMessagesForUser(otherUser)).thenReturn(null);
        when(messageMapper.toResponse(any(Message.class))).thenReturn(new MessageResponse());

        MessageResponse result = conversationService.sendMessage(100L, request);

        assertNotNull(result);
        verify(messagingTemplate).convertAndSendToUser(eq(otherUser.getUsername()), eq("/queue/unread-count"),
                argThat(response -> ((UnreadCountResponse) response).getUnreadCount() == 0L));
    }

    @Test
    @DisplayName("sendMessage_ConversationNotFound")
    void sendMessage_ConversationNotFound() {
        MessageRequest request = new MessageRequest();
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(conversationRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> conversationService.sendMessage(100L, request));
    }

    @Test
    @DisplayName("sendMessage_NoPermission")
    void sendMessage_NoPermission() {
        MessageRequest request = new MessageRequest();
        User stranger = new User();
        stranger.setId(99L);

        when(currentUserService.getCurrentUser()).thenReturn(stranger);
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));

        assertThrows(InvalidResourceException.class, () -> conversationService.sendMessage(100L, request));
    }

    // ==========================================
    // getConversationMessages()
    // ==========================================

    @Test
    @DisplayName("getConversationMessages_Success")
    void getConversationMessages_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));

        Page<Message> page = new PageImpl<>(List.of(message));
        when(messageRepository.findByConversationOrderByCreatedAtDesc(eq(conversation), any(Pageable.class)))
                .thenReturn(page);
        when(messageMapper.toResponse(any(Message.class))).thenReturn(new MessageResponse());

        PageResponse<MessageResponse> result = conversationService.getConversationMessages(100L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
    }

    @Test
    @DisplayName("getConversationMessages_Empty")
    void getConversationMessages_Empty() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));

        Page<Message> page = new PageImpl<>(Collections.emptyList());
        when(messageRepository.findByConversationOrderByCreatedAtDesc(eq(conversation), any(Pageable.class)))
                .thenReturn(page);

        PageResponse<MessageResponse> result = conversationService.getConversationMessages(100L, 0, 10);

        assertNotNull(result);
        assertEquals(0, result.getData().size());
    }

    @Test
    @DisplayName("getConversationMessages_NotFound")
    void getConversationMessages_NotFound() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(conversationRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> conversationService.getConversationMessages(100L, 0, 10));
    }

    @Test
    @DisplayName("getConversationMessages_NoPermission")
    void getConversationMessages_NoPermission() {
        User stranger = new User();
        stranger.setId(99L);
        when(currentUserService.getCurrentUser()).thenReturn(stranger);
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));

        assertThrows(InvalidResourceException.class, () -> conversationService.getConversationMessages(100L, 0, 10));
    }

    // ==========================================
    // markAsRead()
    // ==========================================

    @Test
    @DisplayName("markAsRead_Success")
    void markAsRead_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));
        when(messageRepository.countTotalUnreadMessagesForUser(currentUser)).thenReturn(0L);

        conversationService.markAsRead(100L);

        verify(messageRepository).markMessagesAsRead(conversation, currentUser);
        verify(messagingTemplate).convertAndSendToUser(eq(otherUser.getUsername()), eq("/queue/messages/read"),
                eq(100L));
        verify(messagingTemplate).convertAndSendToUser(eq(currentUser.getUsername()), eq("/queue/unread-count"),
                any(UnreadCountResponse.class));
    }

    @Test
    @DisplayName("markAsRead_UnreadCountNull")
    void markAsRead_UnreadCountNull() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));
        when(messageRepository.countTotalUnreadMessagesForUser(currentUser)).thenReturn(null);

        conversationService.markAsRead(100L);

        verify(messagingTemplate).convertAndSendToUser(eq(currentUser.getUsername()), eq("/queue/unread-count"),
                argThat(response -> ((UnreadCountResponse) response).getUnreadCount() == 0L));
    }

    @Test
    @DisplayName("markAsRead_NotFound")
    void markAsRead_NotFound() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(conversationRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> conversationService.markAsRead(100L));
    }

    @Test
    @DisplayName("markAsRead_NoPermission")
    void markAsRead_NoPermission() {
        User stranger = new User();
        stranger.setId(99L);
        when(currentUserService.getCurrentUser()).thenReturn(stranger);
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));

        assertThrows(InvalidResourceException.class, () -> conversationService.markAsRead(100L));
    }

    // ==========================================
    // getUnreadCount()
    // ==========================================

    @Test
    @DisplayName("getUnreadCount_Success")
    void getUnreadCount_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(messageRepository.countTotalUnreadMessagesForUser(currentUser)).thenReturn(10L);

        UnreadCountResponse result = conversationService.getUnreadCount();

        assertNotNull(result);
        assertEquals(10L, result.getUnreadCount());
    }

    @Test
    @DisplayName("getUnreadCount_NullValue_ReturnZero")
    void getUnreadCount_NullValue_ReturnZero() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(messageRepository.countTotalUnreadMessagesForUser(currentUser)).thenReturn(null);

        UnreadCountResponse result = conversationService.getUnreadCount();

        assertNotNull(result);
        assertEquals(0L, result.getUnreadCount());
    }

    // ==========================================
    // editMessage()
    // ==========================================

    @Test
    @DisplayName("editMessage_Success")
    void editMessage_Success() {
        MessageRequest request = new MessageRequest();
        request.setContent("Edited text");

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(messageRepository.findById(1000L)).thenReturn(Optional.of(message));
        when(messageRepository.save(message)).thenReturn(message);
        when(messageMapper.toResponse(message)).thenReturn(new MessageResponse());

        MessageResponse result = conversationService.editMessage(1000L, request);

        assertNotNull(result);
        assertTrue(message.getIsEdited());
        assertEquals("Edited text", message.getContent());
        verify(messageRepository).save(message);
        verify(messagingTemplate).convertAndSendToUser(eq(otherUser.getUsername()), eq("/queue/messages/update"),
                any(MessageResponse.class));
    }

    @Test
    @DisplayName("editMessage_NotFound")
    void editMessage_NotFound() {
        MessageRequest request = new MessageRequest();
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(messageRepository.findById(1000L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> conversationService.editMessage(1000L, request));
    }

    @Test
    @DisplayName("editMessage_NotSender")
    void editMessage_NotSender() {
        MessageRequest request = new MessageRequest();
        when(currentUserService.getCurrentUser()).thenReturn(otherUser);
        when(messageRepository.findById(1000L)).thenReturn(Optional.of(message));

        assertThrows(InvalidResourceException.class, () -> conversationService.editMessage(1000L, request));
    }

    @Test
    @DisplayName("editMessage_RecalledMessage")
    void editMessage_RecalledMessage() {
        MessageRequest request = new MessageRequest();
        message.setIsRecalled(true);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(messageRepository.findById(1000L)).thenReturn(Optional.of(message));

        assertThrows(InvalidResourceException.class, () -> conversationService.editMessage(1000L, request));
    }

    // ==========================================
    // recallMessage()
    // ==========================================

    @Test
    @DisplayName("recallMessage_Success")
    void recallMessage_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(messageRepository.findById(1000L)).thenReturn(Optional.of(message));
        when(messageRepository.save(message)).thenReturn(message);
        when(messageMapper.toResponse(message)).thenReturn(new MessageResponse());

        MessageResponse result = conversationService.recallMessage(1000L);

        assertNotNull(result);
        assertTrue(message.getIsRecalled());
        assertEquals("Tin nhắn đã bị thu hồi", message.getContent());
        verify(messageRepository).save(message);
        verify(messagingTemplate).convertAndSendToUser(eq(otherUser.getUsername()), eq("/queue/messages/update"),
                any(MessageResponse.class));
    }

    @Test
    @DisplayName("recallMessage_NotFound")
    void recallMessage_NotFound() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(messageRepository.findById(1000L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> conversationService.recallMessage(1000L));
    }

    @Test
    @DisplayName("recallMessage_NotSender")
    void recallMessage_NotSender() {
        when(currentUserService.getCurrentUser()).thenReturn(otherUser);
        when(messageRepository.findById(1000L)).thenReturn(Optional.of(message));

        assertThrows(InvalidResourceException.class, () -> conversationService.recallMessage(1000L));
    }

    @Test
    @DisplayName("recallMessage_AlreadyRecalled")
    void recallMessage_AlreadyRecalled() {
        message.setIsRecalled(true);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(messageRepository.findById(1000L)).thenReturn(Optional.of(message));

        assertThrows(InvalidResourceException.class, () -> conversationService.recallMessage(1000L));
    }

    // ==========================================
    // mapToResponse() coverage via getConversationById
    // ==========================================

    @Test
    @DisplayName("mapToResponse_LastMessageExists")
    void mapToResponse_LastMessageExists() {
        conversation.setLastMessageId(1000L);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));
        when(messageRepository.findById(1000L)).thenReturn(Optional.of(message));

        when(conversationMapper.toResponse(eq(conversation), eq(currentUser), any(), eq("Hello"), eq(1L)))
                .thenReturn(new ConversationResponse());

        ConversationResponse result = conversationService.getConversationById(100L);
        assertNotNull(result);
    }

    @Test
    @DisplayName("mapToResponse_LastMessageNotFound")
    void mapToResponse_LastMessageNotFound() {
        conversation.setLastMessageId(9999L);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));
        when(messageRepository.findById(9999L)).thenReturn(Optional.empty());

        when(conversationMapper.toResponse(eq(conversation), eq(currentUser), any(), isNull(), isNull()))
                .thenReturn(new ConversationResponse());

        ConversationResponse result = conversationService.getConversationById(100L);
        assertNotNull(result);
    }

    @Test
    @DisplayName("mapToResponse_LastMessageIdNull")
    void mapToResponse_LastMessageIdNull() {
        conversation.setLastMessageId(null);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(conversationRepository.findById(100L)).thenReturn(Optional.of(conversation));

        when(conversationMapper.toResponse(eq(conversation), eq(currentUser), any(), isNull(), isNull()))
                .thenReturn(new ConversationResponse());

        ConversationResponse result = conversationService.getConversationById(100L);
        assertNotNull(result);
    }
}
