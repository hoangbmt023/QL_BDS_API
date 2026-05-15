package com.example.qlbds.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import com.example.qlbds.user_service.dto.UserProfileResponse;
import com.example.qlbds.user_service.dto.UserSummaryResponse;
import com.example.qlbds.user_service.entity.Agent;
import com.example.qlbds.user_service.entity.Owner;
import com.example.qlbds.user_service.entity.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserResponseMapper {
    @Mapping(target = "agent", source = "user", qualifiedByName = "mapAgent")
    @Mapping(target = "owner", source = "user", qualifiedByName = "mapOwner")
    UserProfileResponse toUserProfileResponse(User user);

    UserSummaryResponse toUserSummaryResponse(User user);

    @Named("mapSlug")
    default String mapSlug(User user) {
        if (user.getAgent() != null && !user.getAgent().getIsDeleted()) {
            return user.getAgent().getSlug();
        }
        return null;
    }

    @Named("mapAgent")
    default UserProfileResponse.AgentInfo mapAgent(User user) {
        Agent agent = user.getAgent();
        if (agent == null || agent.getIsDeleted()) {
            return null;
        }
        return new UserProfileResponse.AgentInfo(
                agent.getLicenseNumber(),
                agent.getAgencyName(),
                agent.getSlug());
    }

    @Named("mapOwner")
    default UserProfileResponse.OwnerInfo mapOwner(User user) {
        Owner owner = user.getOwner();
        if (owner == null || owner.getIsDeleted()) {
            return null;
        }
        return new UserProfileResponse.OwnerInfo(
                owner.getAddress(),
                owner.getDescription());
    }
}
