package com.example.qlbds.favorite_service.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class DeleteMultipleFavoritesRequest {
    @NotEmpty(message = "Danh sách ID không được để trống")
    private List<Long> propertyIds;
}
