package com.example.qlbds.viewing_service.service;

import com.example.qlbds.common.response.PageResponse;
import com.example.qlbds.viewing_service.dto.ViewingCreateRequest;
import com.example.qlbds.viewing_service.dto.ViewingResponse;
import com.example.qlbds.viewing_service.dto.ViewingStatusRequest;
import com.example.qlbds.viewing_service.dto.ViewingRescheduleRequest;
import com.example.qlbds.viewing_service.dto.ViewingStatsResponse;
import com.example.qlbds.viewing_service.dto.AvailableSlotResponse;
import com.example.qlbds.shared.entity.enums.ViewingStatus;

import java.time.LocalDate;

public interface ViewingService {
    ViewingResponse createViewing(ViewingCreateRequest request);
    PageResponse<ViewingResponse> getMyViewings(int page, int size, ViewingStatus status, boolean upcoming, String sort);
    ViewingResponse updateViewingStatus(Long id, ViewingStatusRequest request);
    
    PageResponse<ViewingResponse> getManagedViewings(int page, int size, ViewingStatus status, boolean upcoming, String sort);
    ViewingResponse getViewingById(Long id);
    ViewingResponse rescheduleViewing(Long id, ViewingRescheduleRequest request);
    ViewingStatsResponse getViewingStats(boolean asManager);
    
    AvailableSlotResponse getAvailableSlots(Long propertyId, LocalDate date);
}
