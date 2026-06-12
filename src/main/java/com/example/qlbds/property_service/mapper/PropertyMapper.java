package com.example.qlbds.property_service.mapper;

import com.example.qlbds.property_service.dto.PropertyResponse;
import com.example.qlbds.property_service.entity.Property;
import com.example.qlbds.user_service.entity.Agent;
import com.example.qlbds.user_service.entity.Owner;
import com.example.qlbds.property_service.entity.PropertyImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PropertyMapper {

    @Mapping(source = "owner", target = "owner")
    @Mapping(source = "agent", target = "agent")
    @Mapping(source = "propertyImages", target = "images")
    @Mapping(source = "viewCount", target = "stats.viewCount")
    @Mapping(source = "favoriteCount", target = "stats.favoriteCount")
    PropertyResponse toResponse(Property property);

    PropertyResponse.ImageInfo toImageInfo(PropertyImage propertyImage);

    // Owner: lấy fullName, phone, email từ User lồng bên trong Owner
    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(source = "user.phone", target = "phone")
    @Mapping(source = "user.email", target = "email")
    PropertyResponse.OwnerInfo toOwnerInfo(Owner owner);

    // Agent: lấy fullName, email từ User; agencyName, licenseNumber, rating, slug
    // từ Agent
    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(source = "user.email", target = "email")
    PropertyResponse.AgentInfo toAgentInfo(Agent agent);
}
