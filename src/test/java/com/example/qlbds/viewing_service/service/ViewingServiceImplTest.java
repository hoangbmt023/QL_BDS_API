package com.example.qlbds.viewing_service.service;

import com.example.qlbds.common.exception.InvalidResourceException;
import com.example.qlbds.common.exception.ResourceNotFoundException;
import com.example.qlbds.common.response.PageResponse;
import com.example.qlbds.config.CurrentUserService;
import com.example.qlbds.property_service.entity.Property;
import com.example.qlbds.property_service.repository.PropertyRepository;
import com.example.qlbds.shared.entity.enums.ViewingStatus;
import com.example.qlbds.user_service.entity.Owner;
import com.example.qlbds.user_service.entity.User;
import com.example.qlbds.viewing_service.dto.*;
import com.example.qlbds.viewing_service.entity.Viewing;
import com.example.qlbds.viewing_service.mapper.ViewingMapper;
import com.example.qlbds.viewing_service.repository.ViewingRepository;
import com.example.qlbds.viewing_service.service.impl.ViewingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ViewingServiceImplTest {

    @Mock
    private ViewingRepository viewingRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ViewingMapper viewingMapper;

    @InjectMocks
    private ViewingServiceImpl viewingService;

    private User currentUser;
    private Property property;
    private Viewing viewing;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);

        property = new Property();
        property.setId(100L);

        viewing = new Viewing();
        viewing.setId(10L);
        viewing.setUser(currentUser);
        viewing.setProperty(property);
        viewing.setStatus(ViewingStatus.PENDING);
        viewing.setScheduledTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
    }

    @Test
    @DisplayName("Đặt lịch thành công khi không có xung đột")
    void createViewing_Success() {
        // Arrange
        ViewingCreateRequest request = new ViewingCreateRequest();
        request.setPropertyId(100L);
        request.setScheduledTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(100L)).thenReturn(Optional.of(property));
        when(viewingRepository.existsConflictingViewing(eq(100L), any(), any(), any(), eq(null))).thenReturn(false);

        Viewing savedViewing = new Viewing();
        when(viewingRepository.save(any(Viewing.class))).thenReturn(savedViewing);
        when(viewingMapper.toResponse(savedViewing)).thenReturn(new ViewingResponse());

        // Act
        ViewingResponse response = viewingService.createViewing(request);

        // Assert
        assertNotNull(response);
        verify(viewingRepository, times(1)).save(any(Viewing.class));
    }

    @Test
    @DisplayName("Đặt lịch thất bại do Bất động sản của chính mình quản lý (Owner)")
    void createViewing_Fail_IsOwner() {
        ViewingCreateRequest request = new ViewingCreateRequest();
        request.setPropertyId(100L);
        request.setScheduledTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));

        Owner owner = new Owner();
        owner.setUser(currentUser);
        property.setOwner(owner);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(100L)).thenReturn(Optional.of(property));

        InvalidResourceException exception = assertThrows(InvalidResourceException.class,
                () -> viewingService.createViewing(request));

        assertTrue(exception.getMessage().contains("không thể đặt lịch xem cho bất động sản do chính mình quản lý"));
    }

    @Test
    @DisplayName("Đặt lịch thất bại do trùng giờ (Conflict)")
    void createViewing_Fail_ConflictTime() {
        ViewingCreateRequest request = new ViewingCreateRequest();
        request.setPropertyId(100L);
        request.setScheduledTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(100L)).thenReturn(Optional.of(property));
        when(viewingRepository.existsConflictingViewing(eq(100L), any(), any(), any(), eq(null))).thenReturn(true);

        InvalidResourceException exception = assertThrows(InvalidResourceException.class,
                () -> viewingService.createViewing(request));

        assertTrue(exception.getMessage().contains("Khung giờ này đã có lịch hẹn"));
        verify(viewingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Người đặt lịch cập nhật trạng thái hủy (CANCELLED) thành công")
    void updateViewingStatus_Booker_Cancel_Success() {
        ViewingStatusRequest request = new ViewingStatusRequest();
        request.setStatus(ViewingStatus.CANCELLED);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));
        when(viewingRepository.save(any(Viewing.class))).thenReturn(viewing);
        when(viewingMapper.toResponse(viewing)).thenReturn(new ViewingResponse());

        ViewingResponse response = viewingService.updateViewingStatus(10L, request);

        assertNotNull(response);
        assertEquals(ViewingStatus.CANCELLED, viewing.getStatus());
        verify(viewingRepository, times(1)).save(viewing);
    }

    @Test
    @DisplayName("Người đặt lịch cố gắng cập nhật thành CONFIRMED sẽ bị báo lỗi")
    void updateViewingStatus_Booker_Confirm_Fail() {
        ViewingStatusRequest request = new ViewingStatusRequest();
        request.setStatus(ViewingStatus.CONFIRMED);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));

        InvalidResourceException exception = assertThrows(InvalidResourceException.class,
                () -> viewingService.updateViewingStatus(10L, request));

        assertTrue(exception.getMessage().contains("chỉ có quyền hủy"));
    }

    @Test
    @DisplayName("Chủ nhà xác nhận lịch (CONFIRMED) thành công")
    void updateViewingStatus_Owner_Confirm_Success() {
        User ownerUser = new User();
        ownerUser.setId(2L);

        Owner owner = new Owner();
        owner.setUser(ownerUser);
        property.setOwner(owner);

        ViewingStatusRequest request = new ViewingStatusRequest();
        request.setStatus(ViewingStatus.CONFIRMED);

        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));
        when(viewingRepository.save(any(Viewing.class))).thenReturn(viewing);

        viewingService.updateViewingStatus(10L, request);

        assertEquals(ViewingStatus.CONFIRMED, viewing.getStatus());
    }

    @Test
    @DisplayName("Người lạ cập nhật trạng thái sẽ bị cấm")
    void updateViewingStatus_Stranger_Fail() {
        User stranger = new User();
        stranger.setId(99L);

        ViewingStatusRequest request = new ViewingStatusRequest();
        request.setStatus(ViewingStatus.CANCELLED);

        when(currentUserService.getCurrentUser()).thenReturn(stranger);
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));

        InvalidResourceException exception = assertThrows(InvalidResourceException.class,
                () -> viewingService.updateViewingStatus(10L, request));

        assertTrue(exception.getMessage().contains("Bạn không có quyền"));
        verify(viewingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đổi giờ thành công khi không có xung đột")
    void rescheduleViewing_Success() {
        ViewingRescheduleRequest request = new ViewingRescheduleRequest();
        request.setScheduledTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0));

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));
        when(viewingRepository.existsConflictingViewing(eq(100L), any(), any(), any(), eq(10L))).thenReturn(false);
        when(viewingRepository.save(any(Viewing.class))).thenReturn(viewing);

        viewingService.rescheduleViewing(10L, request);

        assertEquals(request.getScheduledTime(), viewing.getScheduledTime());
        assertEquals(ViewingStatus.PENDING, viewing.getStatus()); // Đổi giờ xong bị lùi về PENDING
    }

    @Test
    @DisplayName("Lấy danh sách giờ trống thành công")
    void getAvailableSlots_Success() {
        LocalDate date = LocalDate.now().plusDays(1);

        when(propertyRepository.existsByIdAndIsDeletedFalse(100L)).thenReturn(true);
        when(viewingRepository.findByPropertyIdAndScheduledTimeBetweenAndStatusInAndIsDeletedFalse(
                eq(100L), any(), any(), any())).thenReturn(Collections.emptyList());

        AvailableSlotResponse response = viewingService.getAvailableSlots(100L, date);

        assertNotNull(response);
        assertEquals(date, response.getDate());
        assertFalse(response.getAvailableTimes().isEmpty()); // Vì là ngày mai nên mọi slot đều phải trống
    }

    @Test
    @DisplayName("Đặt lịch thất bại do Bất động sản không tồn tại")
    void createViewing_PropertyNotFound() {
        ViewingCreateRequest request = new ViewingCreateRequest();
        request.setPropertyId(999L);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> viewingService.createViewing(request));
    }

    @Test
    @DisplayName("Đặt lịch thất bại do Bất động sản của chính mình quản lý (Agent)")
    void createViewing_Fail_IsAgent() {
        ViewingCreateRequest request = new ViewingCreateRequest();
        request.setPropertyId(100L);
        request.setScheduledTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        com.example.qlbds.user_service.entity.Agent agent = new com.example.qlbds.user_service.entity.Agent();
        agent.setUser(currentUser);
        property.setAgent(agent);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(100L)).thenReturn(Optional.of(property));

        InvalidResourceException exception = assertThrows(InvalidResourceException.class,
                () -> viewingService.createViewing(request));
        assertTrue(exception.getMessage().contains("chính mình quản lý"));
    }

    @Test
    @DisplayName("Đặt lịch thất bại do thời gian trong quá khứ")
    void createViewing_Fail_ScheduledTimeInPast() {
        ViewingCreateRequest request = new ViewingCreateRequest();
        request.setPropertyId(100L);
        request.setScheduledTime(LocalDateTime.now().minusHours(1));

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(100L)).thenReturn(Optional.of(property));

        InvalidResourceException exception = assertThrows(InvalidResourceException.class,
                () -> viewingService.createViewing(request));
        assertTrue(exception.getMessage().contains("phải ở tương lai"));
    }

    @Test
    @DisplayName("Đổi giờ thất bại do bị conflict")
    void rescheduleViewing_Conflict_Fail() {
        ViewingRescheduleRequest request = new ViewingRescheduleRequest();
        request.setScheduledTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0));

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));
        when(viewingRepository.existsConflictingViewing(eq(100L), any(), any(), any(), eq(10L))).thenReturn(true);

        InvalidResourceException exception = assertThrows(InvalidResourceException.class,
                () -> viewingService.rescheduleViewing(10L, request));
        assertTrue(exception.getMessage().contains("đã có lịch hẹn"));
        verify(viewingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đổi giờ thất bại do người khác thực hiện")
    void rescheduleViewing_NotOwner_Fail() {
        ViewingRescheduleRequest request = new ViewingRescheduleRequest();
        User stranger = new User();
        stranger.setId(99L);

        when(currentUserService.getCurrentUser()).thenReturn(stranger);
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));

        InvalidResourceException exception = assertThrows(InvalidResourceException.class,
                () -> viewingService.rescheduleViewing(10L, request));
        assertTrue(exception.getMessage().contains("Chỉ người đặt lịch mới có quyền"));
    }

    @Test
    @DisplayName("Cập nhật trạng thái thất bại do đã COMPLETED")
    void updateViewingStatus_AfterCompleted_Fail() {
        viewing.setStatus(ViewingStatus.COMPLETED);
        ViewingStatusRequest request = new ViewingStatusRequest();
        request.setStatus(ViewingStatus.CANCELLED);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));

        InvalidResourceException exception = assertThrows(InvalidResourceException.class,
                () -> viewingService.updateViewingStatus(10L, request));
        assertTrue(exception.getMessage().contains("đã hoàn thành hoặc đã bị hủy"));
    }

    @Test
    @DisplayName("Cập nhật trạng thái thất bại do đã CANCELLED")
    void updateViewingStatus_AfterCancelled_Fail() {
        viewing.setStatus(ViewingStatus.CANCELLED);
        ViewingStatusRequest request = new ViewingStatusRequest();
        request.setStatus(ViewingStatus.CONFIRMED);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser); // isBooker is true, but booker can only
                                                                           // cancel anyway. Let's test with Owner.

        User ownerUser = new User();
        ownerUser.setId(2L);
        Owner owner = new Owner();
        owner.setUser(ownerUser);
        property.setOwner(owner);
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);

        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));

        InvalidResourceException exception = assertThrows(InvalidResourceException.class,
                () -> viewingService.updateViewingStatus(10L, request));
        assertTrue(exception.getMessage().contains("đã hoàn thành hoặc đã bị hủy"));
    }

    @Test
    @DisplayName("Chủ nhà không thể cập nhật thành PENDING")
    void updateViewingStatus_Owner_InvalidStatus() {
        User ownerUser = new User();
        ownerUser.setId(2L);
        Owner owner = new Owner();
        owner.setUser(ownerUser);
        property.setOwner(owner);
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);

        ViewingStatusRequest request = new ViewingStatusRequest();
        request.setStatus(ViewingStatus.PENDING);
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));

        InvalidResourceException exception = assertThrows(InvalidResourceException.class,
                () -> viewingService.updateViewingStatus(10L, request));
        assertTrue(exception.getMessage().contains("không thể chuyển trạng thái về PENDING"));
    }

    @Test
    @DisplayName("Xem chi tiết lịch hẹn thành công")
    void getViewingById_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));
        when(viewingMapper.toResponse(viewing)).thenReturn(new ViewingResponse());

        ViewingResponse response = viewingService.getViewingById(10L);
        assertNotNull(response);
    }

    @Test
    @DisplayName("Xem chi tiết lịch hẹn thất bại do không có quyền")
    void getViewingById_NoPermission() {
        User stranger = new User();
        stranger.setId(99L);
        when(currentUserService.getCurrentUser()).thenReturn(stranger);
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));

        assertThrows(InvalidResourceException.class, () -> viewingService.getViewingById(10L));
    }

    @Test
    @DisplayName("Lấy danh sách quản lý thành công")
    void getManagedViewings_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        Page<Viewing> page = new PageImpl<>(List.of(viewing));
        when(viewingRepository.findManagedViewingsFiltered(eq(currentUser), any(), anyBoolean(), anyBoolean(), any(), any()))
                .thenReturn(page);

        PageResponse<ViewingResponse> res = viewingService.getManagedViewings(0, 20, null, false, null);
        assertNotNull(res);
    }

    @Test
    @DisplayName("Lấy danh sách giờ trống với các khung giờ đã bị chiếm")
    void getAvailableSlots_WithOccupiedTimes() {
        LocalDate date = LocalDate.now().plusDays(1);
        when(propertyRepository.existsByIdAndIsDeletedFalse(100L)).thenReturn(true);

        // Tạo một lịch hẹn chiếm 09:00
        Viewing booked = new Viewing();
        booked.setScheduledTime(date.atTime(9, 0));

        when(viewingRepository.findByPropertyIdAndScheduledTimeBetweenAndStatusInAndIsDeletedFalse(
                eq(100L), any(), any(), any())).thenReturn(List.of(booked));

        AvailableSlotResponse response = viewingService.getAvailableSlots(100L, date);

        assertNotNull(response);
        // 09:00 và 08:30 và 09:30 có thể bị loại bỏ tuỳ logic (nếu diff < 30 mins thì
        // 08:30 diff = 30 -> ko loại)
        // 09:00 chắc chắn bị loại
        assertFalse(response.getAvailableTimes().contains("09:00"));
        assertTrue(response.getAvailableTimes().contains("08:00"));
        assertTrue(response.getAvailableTimes().contains("10:00"));
    }

    @Test
    @DisplayName("Xem chi tiết lịch hẹn thành công (Chủ nhà)")
    void getViewingById_Owner_Success() {
        User ownerUser = new User(); ownerUser.setId(2L);
        Owner owner = new Owner(); owner.setUser(ownerUser); property.setOwner(owner);
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));
        when(viewingMapper.toResponse(viewing)).thenReturn(new ViewingResponse());

        ViewingResponse response = viewingService.getViewingById(10L);
        assertNotNull(response);
    }

    @Test
    @DisplayName("Xem chi tiết lịch hẹn thành công (Môi giới)")
    void getViewingById_Agent_Success() {
        User agentUser = new User(); agentUser.setId(3L);
        com.example.qlbds.user_service.entity.Agent agent = new com.example.qlbds.user_service.entity.Agent(); agent.setUser(agentUser); property.setAgent(agent);
        when(currentUserService.getCurrentUser()).thenReturn(agentUser);
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));
        when(viewingMapper.toResponse(viewing)).thenReturn(new ViewingResponse());

        ViewingResponse response = viewingService.getViewingById(10L);
        assertNotNull(response);
    }

    @Test
    @DisplayName("Đổi giờ thất bại do lịch đã COMPLETED")
    void rescheduleViewing_AfterCompleted_Fail() {
        viewing.setStatus(ViewingStatus.COMPLETED);
        ViewingRescheduleRequest request = new ViewingRescheduleRequest();
        request.setScheduledTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));

        assertThrows(InvalidResourceException.class, () -> viewingService.rescheduleViewing(10L, request));
        verify(viewingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đổi giờ thất bại do lịch đã CANCELLED")
    void rescheduleViewing_AfterCancelled_Fail() {
        viewing.setStatus(ViewingStatus.CANCELLED);
        ViewingRescheduleRequest request = new ViewingRescheduleRequest();
        request.setScheduledTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));

        assertThrows(InvalidResourceException.class, () -> viewingService.rescheduleViewing(10L, request));
        verify(viewingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đổi giờ thành công từ CONFIRMED sẽ bị reset về PENDING")
    void rescheduleViewing_FromConfirmed_ResetToPending_Success() {
        viewing.setStatus(ViewingStatus.CONFIRMED);
        ViewingRescheduleRequest request = new ViewingRescheduleRequest();
        request.setScheduledTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0));

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));
        when(viewingRepository.existsConflictingViewing(eq(100L), any(), any(), any(), eq(10L))).thenReturn(false);
        when(viewingRepository.save(any(Viewing.class))).thenReturn(viewing);

        viewingService.rescheduleViewing(10L, request);

        assertEquals(ViewingStatus.PENDING, viewing.getStatus());
        verify(viewingRepository, times(1)).save(viewing);
    }

    @Test
    @DisplayName("Lấy danh sách khung giờ trống thất bại do BĐS không tồn tại")
    void getAvailableSlots_PropertyNotFound() {
        when(propertyRepository.existsByIdAndIsDeletedFalse(100L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> viewingService.getAvailableSlots(100L, LocalDate.now()));
    }

    @Test
    @DisplayName("Lấy thống kê thành công (Dưới quyền User)")
    void getViewingStats_AsUser() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        Object[] row1 = {ViewingStatus.PENDING, 5L};
        Object[] row2 = {ViewingStatus.CONFIRMED, 10L};
        when(viewingRepository.countMyViewingsByStatus(currentUser)).thenReturn(Arrays.asList(row1, row2));

        ViewingStatsResponse response = viewingService.getViewingStats(false);
        assertEquals(5L, response.getPending());
        assertEquals(10L, response.getConfirmed());
        assertEquals(15L, response.getTotal());
    }

    @Test
    @DisplayName("Lấy thống kê thành công (Dưới quyền Manager)")
    void getViewingStats_AsManager() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        Object[] row1 = {ViewingStatus.COMPLETED, 3L};
        Object[] row2 = {ViewingStatus.CANCELLED, 2L};
        when(viewingRepository.countManagedViewingsByStatus(currentUser)).thenReturn(Arrays.asList(row1, row2));

        ViewingStatsResponse response = viewingService.getViewingStats(true);
        assertEquals(3L, response.getCompleted());
        assertEquals(2L, response.getCancelled());
        assertEquals(5L, response.getTotal());
    }

    @Test
    @DisplayName("Xem chi tiết lịch hẹn thất bại do không tìm thấy")
    void getViewingById_NotFound() {
        when(viewingRepository.findByIdAndIsDeletedFalse(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> viewingService.getViewingById(999L));
    }

    @Test
    @DisplayName("Cập nhật trạng thái thất bại do không tìm thấy")
    void updateViewingStatus_NotFound() {
        ViewingStatusRequest request = new ViewingStatusRequest();
        request.setStatus(ViewingStatus.CONFIRMED);
        when(viewingRepository.findByIdAndIsDeletedFalse(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> viewingService.updateViewingStatus(999L, request));
    }

    @Test
    @DisplayName("Đổi giờ thất bại do không tìm thấy")
    void rescheduleViewing_NotFound() {
        ViewingRescheduleRequest request = new ViewingRescheduleRequest();
        request.setScheduledTime(LocalDateTime.now().plusDays(2).withHour(14).withMinute(0));
        when(viewingRepository.findByIdAndIsDeletedFalse(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> viewingService.rescheduleViewing(999L, request));
    }

    @Test
    @DisplayName("Đặt lịch thất bại do chưa truyền thời gian hẹn")
    void createViewing_NullScheduledTime_Fail() {
        ViewingCreateRequest request = new ViewingCreateRequest();
        request.setPropertyId(100L);

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(100L)).thenReturn(Optional.of(property));

        InvalidResourceException exception = assertThrows(InvalidResourceException.class,
                () -> viewingService.createViewing(request));
        assertTrue(exception.getMessage().contains("không được để trống"));
    }

    @Test
    @DisplayName("Đổi giờ thất bại do thời gian trong quá khứ")
    void rescheduleViewing_PastTime_Fail() {
        ViewingRescheduleRequest request = new ViewingRescheduleRequest();
        request.setScheduledTime(LocalDateTime.now().minusHours(1));

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));

        InvalidResourceException exception = assertThrows(InvalidResourceException.class, 
            () -> viewingService.rescheduleViewing(10L, request));
        assertTrue(exception.getMessage().contains("phải ở tương lai"));
    }

    @Test
    @DisplayName("Lấy thống kê thành công khi chưa có lịch hẹn nào")
    void getViewingStats_Empty() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(viewingRepository.countMyViewingsByStatus(currentUser)).thenReturn(Collections.emptyList());

        ViewingStatsResponse response = viewingService.getViewingStats(false);
        assertEquals(0L, response.getPending());
        assertEquals(0L, response.getConfirmed());
        assertEquals(0L, response.getCompleted());
        assertEquals(0L, response.getCancelled());
        assertEquals(0L, response.getTotal());
    }

    @Test
    @DisplayName("Chủ nhà xác nhận lịch đã COMPLETED thành công")
    void updateViewingStatus_Owner_Completed_Success() {
        User ownerUser = new User(); ownerUser.setId(2L);
        Owner owner = new Owner(); owner.setUser(ownerUser); property.setOwner(owner);
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        
        viewing.setStatus(ViewingStatus.CONFIRMED); // Phải xác nhận trước mới được hoàn thành
        ViewingStatusRequest request = new ViewingStatusRequest(); request.setStatus(ViewingStatus.COMPLETED);
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));

        viewingService.updateViewingStatus(10L, request);
        assertEquals(ViewingStatus.COMPLETED, viewing.getStatus());
    }

    @Test
    @DisplayName("Lấy danh sách quản lý rỗng")
    void getManagedViewings_Empty() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        Page<Viewing> page = new PageImpl<>(Collections.emptyList());
        when(viewingRepository.findManagedViewingsFiltered(eq(currentUser), any(), anyBoolean(), anyBoolean(), any(), any())).thenReturn(page);
        
        PageResponse<ViewingResponse> res = viewingService.getManagedViewings(0, 20, null, false, "scheduledTime,desc");
        assertNotNull(res);
        assertTrue(res.getData() == null || res.getData().isEmpty());
    }

    // --- BỔ SUNG CÁC TEST CASE ĐỂ TĂNG COVERAGE ---

    @Test
    @DisplayName("Lấy danh sách lịch xem của tôi thành công")
    void getMyViewings_Success() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        Page<Viewing> page = new PageImpl<>(List.of(viewing));
        when(viewingRepository.findMyViewingsFiltered(eq(currentUser), any(), anyBoolean(), anyBoolean(), any(), any()))
                .thenReturn(page);

        PageResponse<ViewingResponse> res = viewingService.getMyViewings(0, 20, null, false, "scheduledTime,asc");
        assertNotNull(res);
        assertFalse(res.getData().isEmpty());
    }

    @Test
    @DisplayName("Lấy danh sách lịch xem của tôi rỗng")
    void getMyViewings_Empty() {
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        Page<Viewing> page = new PageImpl<>(Collections.emptyList());
        when(viewingRepository.findMyViewingsFiltered(eq(currentUser), any(), anyBoolean(), anyBoolean(), any(), any()))
                .thenReturn(page);

        PageResponse<ViewingResponse> res = viewingService.getMyViewings(0, 20, ViewingStatus.CONFIRMED, true, "scheduledTime,desc");
        assertNotNull(res);
        assertTrue(res.getData().isEmpty());
    }

    @Test
    @DisplayName("Chủ nhà xác nhận lịch nhưng không phải trạng thái PENDING thì ném lỗi")
    void updateViewingStatus_Confirm_NonPending_Fail() {
        User ownerUser = new User(); ownerUser.setId(2L);
        Owner owner = new Owner(); owner.setUser(ownerUser); property.setOwner(owner);
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        
        viewing.setStatus(ViewingStatus.CONFIRMED);
        ViewingStatusRequest request = new ViewingStatusRequest(); 
        request.setStatus(ViewingStatus.CONFIRMED);
        
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));

        InvalidResourceException exception = assertThrows(InvalidResourceException.class, 
            () -> viewingService.updateViewingStatus(10L, request));
        assertTrue(exception.getMessage().contains("đang ở trạng thái chờ"));
    }

    @Test
    @DisplayName("Chủ nhà hoàn thành lịch nhưng chưa xác nhận (từ PENDING) thì ném lỗi")
    void updateViewingStatus_Completed_FromPending_Fail() {
        User ownerUser = new User(); ownerUser.setId(2L);
        Owner owner = new Owner(); owner.setUser(ownerUser); property.setOwner(owner);
        when(currentUserService.getCurrentUser()).thenReturn(ownerUser);
        
        viewing.setStatus(ViewingStatus.PENDING);
        ViewingStatusRequest request = new ViewingStatusRequest(); 
        request.setStatus(ViewingStatus.COMPLETED);
        
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));

        InvalidResourceException exception = assertThrows(InvalidResourceException.class, 
            () -> viewingService.updateViewingStatus(10L, request));
        assertTrue(exception.getMessage().contains("đã được xác nhận"));
    }

    @Test
    @DisplayName("Đặt lịch trước 8h sáng ném lỗi")
    void createViewing_Fail_BeforeWorkingHour() {
        ViewingCreateRequest request = new ViewingCreateRequest();
        request.setPropertyId(100L);
        request.setScheduledTime(LocalDateTime.now().plusDays(1).withHour(7).withMinute(0));

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(100L)).thenReturn(Optional.of(property));

        InvalidResourceException exception = assertThrows(InvalidResourceException.class, () -> viewingService.createViewing(request));
        assertTrue(exception.getMessage().contains("trong giờ làm việc"));
    }

    @Test
    @DisplayName("Đặt lịch sau 17h chiều ném lỗi")
    void createViewing_Fail_AfterWorkingHour() {
        ViewingCreateRequest request = new ViewingCreateRequest();
        request.setPropertyId(100L);
        request.setScheduledTime(LocalDateTime.now().plusDays(1).withHour(17).withMinute(30));

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(100L)).thenReturn(Optional.of(property));

        InvalidResourceException exception = assertThrows(InvalidResourceException.class, () -> viewingService.createViewing(request));
        assertTrue(exception.getMessage().contains("trong giờ làm việc"));
    }

    @Test
    @DisplayName("Đặt lịch phút không chẵn (10:15) ném lỗi")
    void createViewing_Fail_InvalidMinute() {
        ViewingCreateRequest request = new ViewingCreateRequest();
        request.setPropertyId(100L);
        request.setScheduledTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(15));

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(100L)).thenReturn(Optional.of(property));

        InvalidResourceException exception = assertThrows(InvalidResourceException.class, () -> viewingService.createViewing(request));
        assertTrue(exception.getMessage().contains("chẵn theo mỗi 30 phút"));
    }

    @Test
    @DisplayName("Đổi lịch sang ngoài giờ làm việc ném lỗi")
    void rescheduleViewing_InvalidWorkingHour_Fail() {
        ViewingRescheduleRequest request = new ViewingRescheduleRequest();
        request.setScheduledTime(LocalDateTime.now().plusDays(1).withHour(18).withMinute(0));

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));

        InvalidResourceException exception = assertThrows(InvalidResourceException.class, () -> viewingService.rescheduleViewing(10L, request));
        assertTrue(exception.getMessage().contains("trong giờ làm việc"));
    }

    @Test
    @DisplayName("Đổi lịch sang phút không chẵn ném lỗi")
    void rescheduleViewing_InvalidMinute_Fail() {
        ViewingRescheduleRequest request = new ViewingRescheduleRequest();
        request.setScheduledTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(45));

        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));

        InvalidResourceException exception = assertThrows(InvalidResourceException.class, () -> viewingService.rescheduleViewing(10L, request));
        assertTrue(exception.getMessage().contains("chẵn theo mỗi 30 phút"));
    }

    @Test
    @DisplayName("Lấy danh sách giờ trống mà không truyền date (date null)")
    void getAvailableSlots_NullDate() {
        when(propertyRepository.existsByIdAndIsDeletedFalse(100L)).thenReturn(true);
        when(viewingRepository.findByPropertyIdAndScheduledTimeBetweenAndStatusInAndIsDeletedFalse(
                eq(100L), any(), any(), any())).thenReturn(Collections.emptyList());

        AvailableSlotResponse response = viewingService.getAvailableSlots(100L, null);

        assertNotNull(response);
        assertEquals(LocalDate.now(), response.getDate());
    }

    @Test
    @DisplayName("Môi giới xác nhận lịch thành công")
    void updateViewingStatus_Agent_Confirm_Success() {
        User agentUser = new User(); agentUser.setId(3L);
        com.example.qlbds.user_service.entity.Agent agent = new com.example.qlbds.user_service.entity.Agent(); 
        agent.setUser(agentUser); 
        property.setAgent(agent);
        when(currentUserService.getCurrentUser()).thenReturn(agentUser);
        
        viewing.setStatus(ViewingStatus.PENDING);
        ViewingStatusRequest request = new ViewingStatusRequest(); 
        request.setStatus(ViewingStatus.CONFIRMED);
        
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));
        when(viewingRepository.save(any(Viewing.class))).thenReturn(viewing);

        viewingService.updateViewingStatus(10L, request);
        assertEquals(ViewingStatus.CONFIRMED, viewing.getStatus());
    }

    @Test
    @DisplayName("Môi giới hoàn thành lịch thành công")
    void updateViewingStatus_Agent_Completed_Success() {
        User agentUser = new User(); agentUser.setId(3L);
        com.example.qlbds.user_service.entity.Agent agent = new com.example.qlbds.user_service.entity.Agent(); 
        agent.setUser(agentUser); 
        property.setAgent(agent);
        when(currentUserService.getCurrentUser()).thenReturn(agentUser);
        
        viewing.setStatus(ViewingStatus.CONFIRMED);
        ViewingStatusRequest request = new ViewingStatusRequest(); 
        request.setStatus(ViewingStatus.COMPLETED);
        
        when(viewingRepository.findByIdAndIsDeletedFalse(10L)).thenReturn(Optional.of(viewing));
        when(viewingRepository.save(any(Viewing.class))).thenReturn(viewing);

        viewingService.updateViewingStatus(10L, request);
        assertEquals(ViewingStatus.COMPLETED, viewing.getStatus());
    }
}
