package com.example.qlbds.conversation_service.mapper;

import com.example.qlbds.conversation_service.dto.MessageResponse;
import com.example.qlbds.conversation_service.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    @Mapping(target = "conversationId", source = "conversation.id")
    @Mapping(target = "senderId", source = "sender.id")
    MessageResponse toResponse(Message message);
}
