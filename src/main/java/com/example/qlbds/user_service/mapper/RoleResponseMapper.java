package com.example.qlbds.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.example.qlbds.user_service.dto.AgentRequestResponse;
import com.example.qlbds.user_service.dto.OwnerResponse;
import com.example.qlbds.user_service.entity.AgentRequest;
import com.example.qlbds.user_service.entity.Owner;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RoleResponseMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "fullName", source = "user.fullName")
    OwnerResponse toOwnerResponse(Owner owner);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "slug", source = "user.agent.slug")
    AgentRequestResponse toAgentRequestResponse(AgentRequest agentRequest);
}
