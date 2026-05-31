package com.example.qlbds.viewing_service.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ViewingRescheduleRequest {
    @NotNull(message = "Thời gian xem nhà không được để trống")
    @Future(message = "Thời gian xem nhà phải ở tương lai")
    private LocalDateTime scheduledTime;

    private String note;
}
