package com.example.qlbds.favorite_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteRequest {
    @NotNull(message = "Property ID không được để trống")
    private Long propertyId;
}
