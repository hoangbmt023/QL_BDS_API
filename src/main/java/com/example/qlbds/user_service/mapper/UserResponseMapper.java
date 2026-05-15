package com.example.qlbds.user_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.example.qlbds.user_service.dto.UserProfileResponse;
import com.example.qlbds.user_service.dto.UserSummaryResponse;
import com.example.qlbds.user_service.entity.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserResponseMapper {
    
    UserProfileResponse toUserProfileResponse(User user);
    
    UserSummaryResponse toUserSummaryResponse(User user);
}
