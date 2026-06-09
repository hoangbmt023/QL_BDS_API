package com.example.qlbds.viewing_service.service.impl;

import com.example.qlbds.common.exception.InvalidResourceException;
import com.example.qlbds.common.exception.ResourceNotFoundException;
import com.example.qlbds.common.response.PageResponse;
import com.example.qlbds.config.CurrentUserService;
import com.example.qlbds.property_service.entity.Property;
import com.example.qlbds.property_service.repository.PropertyRepository;
import com.example.qlbds.shared.entity.enums.ViewingStatus;
import com.example.qlbds.user_service.entity.User;
import com.example.qlbds.viewing_service.dto.ViewingCreateRequest;
import com.example.qlbds.viewing_service.dto.ViewingResponse;
import com.example.qlbds.viewing_service.dto.ViewingStatusRequest;
import com.example.qlbds.viewing_service.entity.Viewing;
import com.example.qlbds.viewing_service.mapper.ViewingMapper;
import com.example.qlbds.viewing_service.repository.ViewingRepository;
import com.example.qlbds.viewing_service.service.ViewingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.example.qlbds.viewing_service.dto.ViewingRescheduleRequest;
import com.example.qlbds.viewing_service.dto.ViewingStatsResponse;
import com.example.qlbds.viewing_service.dto.AvailableSlotResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ViewingServiceImpl implements ViewingService {

    private final ViewingRepository viewingRepository;
    private final PropertyRepository propertyRepository;
    private final CurrentUserService currentUserService;
    private final ViewingMapper viewingMapper;

    @Override
    @Transactional
    public ViewingResponse createViewing(ViewingCreateRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        
        Property property = propertyRepository.findByIdAndIsDeletedFalse(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bất động sản với ID: " + request.getPropertyId()));
                
        boolean isPropertyOwner = property.getOwner() != null && 
                                  property.getOwner().getUser().getId().equals(currentUser.getId());
        boolean isPropertyAgent = property.getAgent() != null && 
                                  property.getAgent().getUser().getId().equals(currentUser.getId());
                                  
        if (isPropertyOwner || isPropertyAgent) {
            throw new InvalidResourceException("Bạn không thể đặt lịch xem cho bất động sản do chính mình quản lý.");
        }
                
        if (request.getScheduledTime() == null) {
            throw new InvalidResourceException("Thời gian hẹn không được để trống.");
        }
                
        if (request.getScheduledTime().isBefore(LocalDateTime.now())) {
            throw new InvalidResourceException("Thời gian xem nhà phải ở tương lai.");
        }
                
        LocalTime time = request.getScheduledTime().toLocalTime();
        if (time.isBefore(LocalTime.of(8, 0)) || time.isAfter(LocalTime.of(17, 0))) {
            throw new InvalidResourceException("Thời gian hẹn phải nằm trong giờ làm việc (08:00 - 17:00).");
        }
        if (time.getMinute() != 0 && time.getMinute() != 30) {
            throw new InvalidResourceException("Thời gian hẹn phải chẵn theo mỗi 30 phút (VD: 08:00, 08:30).");
        }
                
        // Conflict validation: 30 minutes before and after
        LocalDateTime startTime = request.getScheduledTime().minusMinutes(30);
        LocalDateTime endTime = request.getScheduledTime().plusMinutes(30);
        
        List<ViewingStatus> activeStatuses = Arrays.asList(ViewingStatus.PENDING, ViewingStatus.CONFIRMED);
        
        boolean isConflict = viewingRepository.existsConflictingViewing(
                property.getId(), startTime, endTime, activeStatuses, null);
                
        if (isConflict) {
            throw new InvalidResourceException("Khung giờ này đã có lịch hẹn. Vui lòng chọn thời gian khác cách ít nhất 30 phút.");
        }
        
        Viewing viewing = Viewing.builder()
                .property(property)
                .user(currentUser)
                .scheduledTime(request.getScheduledTime())
                .note(request.getNote())
                .status(ViewingStatus.PENDING)
                .build();
                
        viewing = viewingRepository.save(viewing);
        return viewingMapper.toResponse(viewing);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ViewingResponse> getMyViewings(int page, int size, ViewingStatus status, boolean upcoming, String sortParam) {
        User currentUser = currentUserService.getCurrentUser();
        Sort sort = parseSort(sortParam);
        Pageable pageable = PageRequest.of(page, size, sort);
        LocalDateTime now = LocalDateTime.now();
        
        ViewingStatus queryStatus = status != null ? status : ViewingStatus.PENDING;
        Page<Viewing> viewingPage = viewingRepository.findMyViewingsFiltered(currentUser, queryStatus, status != null, upcoming, now, pageable);
        
        return PageResponse.from(viewingPage.map(viewingMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ViewingResponse> getManagedViewings(int page, int size, ViewingStatus status, boolean upcoming, String sortParam) {
        User currentUser = currentUserService.getCurrentUser();
        Sort sort = parseSort(sortParam);
        Pageable pageable = PageRequest.of(page, size, sort);
        LocalDateTime now = LocalDateTime.now();
        
        ViewingStatus queryStatus = status != null ? status : ViewingStatus.PENDING;
        Page<Viewing> viewingPage = viewingRepository.findManagedViewingsFiltered(currentUser, queryStatus, status != null, upcoming, now, pageable);
        
        return PageResponse.from(viewingPage.map(viewingMapper::toResponse));
    }
    
    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "scheduledTime");
        }
        String[] parts = sortParam.split(",");
        String property = parts[0];
        Sort.Direction direction = parts.length > 1 && parts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }

    @Override
    @Transactional(readOnly = true)
    public ViewingResponse getViewingById(Long id) {
        User currentUser = currentUserService.getCurrentUser();
        Viewing viewing = viewingRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn xem nhà với ID: " + id));
                
        boolean isBooker = viewing.getUser().getId().equals(currentUser.getId());
        boolean isPropertyOwner = viewing.getProperty().getOwner() != null && 
                                  viewing.getProperty().getOwner().getUser().getId().equals(currentUser.getId());
        boolean isPropertyAgent = viewing.getProperty().getAgent() != null && 
                                  viewing.getProperty().getAgent().getUser().getId().equals(currentUser.getId());
                                  
        if (!isBooker && !isPropertyOwner && !isPropertyAgent) {
            throw new InvalidResourceException("Bạn không có quyền xem thông tin lịch hẹn này.");
        }
        
        return viewingMapper.toResponse(viewing);
    }

    @Override
    @Transactional
    public ViewingResponse rescheduleViewing(Long id, ViewingRescheduleRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        Viewing viewing = viewingRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn xem nhà với ID: " + id));
                
        boolean isBooker = viewing.getUser().getId().equals(currentUser.getId());
        
        if (!isBooker) {
            throw new InvalidResourceException("Chỉ người đặt lịch mới có quyền đổi giờ xem nhà.");
        }
        
        if (viewing.getStatus() == ViewingStatus.CANCELLED || viewing.getStatus() == ViewingStatus.COMPLETED) {
            throw new InvalidResourceException("Không thể đổi giờ của lịch hẹn đã hoàn thành hoặc đã bị hủy.");
        }
        
        if (request.getScheduledTime() == null) {
            throw new InvalidResourceException("Thời gian đổi không được để trống.");
        }
        
        if (request.getScheduledTime().isBefore(LocalDateTime.now())) {
            throw new InvalidResourceException("Thời gian xem nhà phải ở tương lai.");
        }
        
        LocalTime time = request.getScheduledTime().toLocalTime();
        if (time.isBefore(LocalTime.of(8, 0)) || time.isAfter(LocalTime.of(17, 0))) {
            throw new InvalidResourceException("Thời gian hẹn phải nằm trong giờ làm việc (08:00 - 17:00).");
        }
        if (time.getMinute() != 0 && time.getMinute() != 30) {
            throw new InvalidResourceException("Thời gian hẹn phải chẵn theo mỗi 30 phút (VD: 08:00, 08:30).");
        }
        
        LocalDateTime startTime = request.getScheduledTime().minusMinutes(30);
        LocalDateTime endTime = request.getScheduledTime().plusMinutes(30);
        
        List<ViewingStatus> activeStatuses = Arrays.asList(ViewingStatus.PENDING, ViewingStatus.CONFIRMED);
        
        boolean isConflict = viewingRepository.existsConflictingViewing(
                viewing.getProperty().getId(), startTime, endTime, activeStatuses, viewing.getId());
                
        if (isConflict) {
            throw new InvalidResourceException("Khung giờ này đã có lịch hẹn. Vui lòng chọn thời gian khác cách ít nhất 30 phút.");
        }
        
        viewing.setScheduledTime(request.getScheduledTime());
        if (request.getNote() != null) {
            viewing.setNote(request.getNote());
        }
        // Khi user đổi giờ, reset status về PENDING để chờ xác nhận lại
        viewing.setStatus(ViewingStatus.PENDING); 
        
        viewing = viewingRepository.save(viewing);
        return viewingMapper.toResponse(viewing);
    }

    @Override
    @Transactional(readOnly = true)
    public ViewingStatsResponse getViewingStats(boolean asManager) {
        User currentUser = currentUserService.getCurrentUser();
        List<Object[]> statsRaw;
        
        if (asManager) {
            statsRaw = viewingRepository.countManagedViewingsByStatus(currentUser);
        } else {
            statsRaw = viewingRepository.countMyViewingsByStatus(currentUser);
        }
        
        ViewingStatsResponse stats = new ViewingStatsResponse();
        long total = 0;
        
        for (Object[] row : statsRaw) {
            ViewingStatus status = (ViewingStatus) row[0];
            long count = ((Number) row[1]).longValue();
            
            switch (status) {
                case PENDING: stats.setPending(count); break;
                case CONFIRMED: stats.setConfirmed(count); break;
                case COMPLETED: stats.setCompleted(count); break;
                case CANCELLED: stats.setCancelled(count); break;
            }
            total += count;
        }
        stats.setTotal(total);
        return stats;
    }

    @Override
    @Transactional
    public ViewingResponse updateViewingStatus(Long id, ViewingStatusRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        
        Viewing viewing = viewingRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn xem nhà với ID: " + id));
                
        boolean isBooker = viewing.getUser().getId().equals(currentUser.getId());
        
        boolean isPropertyOwner = viewing.getProperty().getOwner() != null && 
                                  viewing.getProperty().getOwner().getUser().getId().equals(currentUser.getId());
                                  
        boolean isPropertyAgent = viewing.getProperty().getAgent() != null && 
                                  viewing.getProperty().getAgent().getUser().getId().equals(currentUser.getId());
                                  
        if (!isBooker && !isPropertyOwner && !isPropertyAgent) {
            throw new InvalidResourceException("Bạn không có quyền cập nhật trạng thái lịch hẹn này.");
        }
        
        if (isBooker && request.getStatus() != ViewingStatus.CANCELLED) {
            throw new InvalidResourceException("Người đặt lịch chỉ có quyền hủy (CANCELLED) lịch hẹn.");
        }
        
        if (!isBooker && request.getStatus() == ViewingStatus.PENDING) {
            throw new InvalidResourceException("Chủ nhà/Môi giới không thể chuyển trạng thái về PENDING.");
        }
        
        if (viewing.getStatus() == ViewingStatus.CANCELLED || viewing.getStatus() == ViewingStatus.COMPLETED) {
            throw new InvalidResourceException("Không thể thay đổi trạng thái của lịch hẹn đã hoàn thành hoặc đã bị hủy.");
        }
        
        if (request.getStatus() == ViewingStatus.CONFIRMED && viewing.getStatus() != ViewingStatus.PENDING) {
            throw new InvalidResourceException("Chỉ có thể xác nhận (CONFIRMED) lịch hẹn đang ở trạng thái chờ (PENDING).");
        }
        
        if (request.getStatus() == ViewingStatus.COMPLETED && viewing.getStatus() != ViewingStatus.CONFIRMED) {
            throw new InvalidResourceException("Chỉ có thể hoàn thành (COMPLETED) lịch hẹn đã được xác nhận (CONFIRMED).");
        }
        
        viewing.setStatus(request.getStatus());
        viewing = viewingRepository.save(viewing);
        
        return viewingMapper.toResponse(viewing);
    }

    @Override
    @Transactional(readOnly = true)
    public AvailableSlotResponse getAvailableSlots(Long propertyId, LocalDate date) {
        if (!propertyRepository.existsByIdAndIsDeletedFalse(propertyId)) {
            throw new ResourceNotFoundException("Không tìm thấy bất động sản với ID: " + propertyId);
        }
        
        if (date == null) {
            date = LocalDate.now();
        }
        
        // Define working hours: 08:00 to 17:00
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        
        List<ViewingStatus> activeStatuses = Arrays.asList(ViewingStatus.PENDING, ViewingStatus.CONFIRMED);
        
        List<Viewing> bookedViewings = viewingRepository.findByPropertyIdAndScheduledTimeBetweenAndStatusInAndIsDeletedFalse(
                propertyId, startOfDay, endOfDay, activeStatuses);
                
        List<String> availableTimes = new ArrayList<>();
        
        LocalTime currentSlot = startTime;
        while (!currentSlot.isAfter(endTime)) {
            LocalDateTime slotDateTime = date.atTime(currentSlot);
            
            // Allow only future slots if it's today
            if (slotDateTime.isAfter(LocalDateTime.now())) {
                boolean isBooked = false;
                for (Viewing v : bookedViewings) {
                    LocalDateTime bookedTime = v.getScheduledTime();
                    long diffMinutes = Duration.between(slotDateTime, bookedTime).abs().toMinutes();
                    if (diffMinutes < 30) {
                        isBooked = true;
                        break;
                    }
                }
                
                if (!isBooked) {
                    // String format "HH:mm"
                    availableTimes.add(String.format("%02d:%02d", currentSlot.getHour(), currentSlot.getMinute()));
                }
            }
            currentSlot = currentSlot.plusMinutes(30); // 30-minute intervals
        }
        
        return new AvailableSlotResponse(date, availableTimes);
    }
}
