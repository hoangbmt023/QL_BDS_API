package com.example.qlbds.favorite_service.dto;

import com.example.qlbds.property_service.dto.PropertyResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteResponse {
    private Long id;
    private LocalDateTime savedAt;
    private PropertyResponse property;
}
