package com.example.qlbds.viewing_service.mapper;

import com.example.qlbds.property_service.mapper.PropertyMapper;
import com.example.qlbds.viewing_service.dto.ViewingResponse;
import com.example.qlbds.viewing_service.entity.Viewing;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PropertyMapper.class})
public interface ViewingMapper {

    @Mapping(source = "user.id", target = "user.id")
    @Mapping(source = "user.fullName", target = "user.fullName")
    @Mapping(source = "user.phone", target = "user.phone")
    @Mapping(source = "user.email", target = "user.email")
    ViewingResponse toResponse(Viewing viewing);
}
