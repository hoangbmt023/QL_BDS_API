package com.example.qlbds.property_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.qlbds.property_service.dto.PropertyResponse;
import com.example.qlbds.property_service.entity.Property;
import com.example.qlbds.user_service.entity.Agent;
import com.example.qlbds.user_service.entity.Owner;

@Mapper(componentModel = "spring")
public interface PropertyMapper {

    @Mapping(source = "owner", target = "owner")
    @Mapping(source = "agent", target = "agent")
    PropertyResponse toResponse(Property property);

    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(source = "user.phone", target = "phone")
    PropertyResponse.OwnerInfo toOwnerInfo(Owner owner);

    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "agent.agencyName", target = "agencyName")
    @Mapping(source = "agent.licenseNumber", target = "licenseNumber")
    @Mapping(source = "agent.rating", target = "rating")
    @Mapping(source = "agent.slug", target = "slug")
    default PropertyResponse.AgentInfo toAgentInfo(Agent agent) {
        if (agent == null) {
            return null;
        }

        PropertyResponse.AgentInfo agentInfo = PropertyResponse.AgentInfo.builder()
                .id(agent.getUser().getId())
                .username(agent.getUser().getUsername())
                .email(agent.getUser().getEmail())
                .agencyName(agent.getAgencyName())
                .licenseNumber(agent.getLicenseNumber())
                .rating(agent.getRating())
                .slug(agent.getSlug())
                .build();

        return agentInfo;
    }
}
