package com.example.qlbds.favorite_service.mapper;

import com.example.qlbds.favorite_service.dto.FavoriteResponse;
import com.example.qlbds.favorite_service.entity.Favorite;
import com.example.qlbds.property_service.mapper.PropertyMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {PropertyMapper.class})
public interface FavoriteMapper {

    @Mapping(source = "createdAt", target = "savedAt")
    FavoriteResponse toResponse(Favorite favorite);
}
