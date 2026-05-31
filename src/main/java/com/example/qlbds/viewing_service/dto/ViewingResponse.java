package com.example.qlbds.viewing_service.dto;

import com.example.qlbds.property_service.dto.PropertyResponse;
import com.example.qlbds.shared.entity.enums.ViewingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewingResponse {
    private Long id;
    private PropertyResponse property;
    private UserInfo user;
    private LocalDateTime scheduledTime;
    private ViewingStatus status;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String fullName;
        private String phone;
        private String email;
    }
}
