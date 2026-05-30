package com.example.qlbds.favorite_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteActionResponse {
    private Long propertyId;
    private Boolean isFavorite;
    private Integer favoriteCount;
}
