package com.example.qlbds.conversation_service.mapper;

import com.example.qlbds.conversation_service.dto.ConversationResponse;
import com.example.qlbds.conversation_service.entity.Conversation;
import com.example.qlbds.property_service.mapper.PropertyMapper;
import com.example.qlbds.user_service.entity.User;
import com.example.qlbds.user_service.mapper.UserResponseMapper;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ConversationMapper {

    @Autowired
    protected PropertyMapper propertyMapper;

    @Autowired
    protected UserResponseMapper userResponseMapper;

    public ConversationResponse toResponse(Conversation conversation, User currentUser, Long unreadCount, String lastMessage, Long lastMessageSenderId) {
        if (conversation == null) {
            return null;
        }

        ConversationResponse response = new ConversationResponse();
        response.setId(conversation.getId());
        response.setProperty(propertyMapper.toResponse(conversation.getProperty()));

        User otherUser = conversation.getUserOne().getId().equals(currentUser.getId()) ? conversation.getUserTwo() : conversation.getUserOne();
        response.setOtherParticipant(userResponseMapper.toUserSummaryResponse(otherUser));
        
        response.setParticipants(List.of(
                userResponseMapper.toUserSummaryResponse(conversation.getUserOne()),
                userResponseMapper.toUserSummaryResponse(conversation.getUserTwo())
        ));

        response.setLastMessage(lastMessage);
        response.setLastMessageSenderId(lastMessageSenderId);
        response.setLastMessageAt(conversation.getLastMessageAt());
        response.setUnreadCount(unreadCount);

        return response;
    }
}
