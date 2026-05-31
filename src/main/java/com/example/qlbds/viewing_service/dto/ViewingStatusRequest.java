package com.example.qlbds.viewing_service.dto;

import com.example.qlbds.shared.entity.enums.ViewingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ViewingStatusRequest {
    @NotNull(message = "Trạng thái không được để trống")
    private ViewingStatus status;
}
