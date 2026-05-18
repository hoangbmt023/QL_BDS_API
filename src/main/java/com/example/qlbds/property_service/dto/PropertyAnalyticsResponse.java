package com.example.qlbds.property_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyAnalyticsResponse {
    private Long propertyId;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer viewingAppointments; // mock or fetch from viewing_service
    private Integer totalConversations; // mock or fetch from conversation_service
    private Double conversionRate; // (viewingAppointments / viewCount) * 100
}
