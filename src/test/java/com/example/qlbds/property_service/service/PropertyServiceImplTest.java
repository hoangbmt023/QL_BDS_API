package com.example.qlbds.property_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.example.qlbds.common.exception.ResourceNotFoundException;
import com.example.qlbds.conversation_service.repository.ConversationRepository;
import com.example.qlbds.property_service.dto.*;
import com.example.qlbds.property_service.entity.Property;
import com.example.qlbds.property_service.entity.PropertyImage;
import com.example.qlbds.property_service.mapper.PropertyMapper;
import com.example.qlbds.property_service.repository.PropertyImageRepository;
import com.example.qlbds.property_service.repository.PropertyRepository;
import com.example.qlbds.property_service.service.impl.PropertyServiceImpl;
import com.example.qlbds.shared.entity.enums.PropertyStatus;
import com.example.qlbds.shared.service.FileUploadService;
import com.example.qlbds.shared.service.SlugService;
import com.example.qlbds.user_service.entity.Owner;
import com.example.qlbds.user_service.repository.AgentRepository;
import com.example.qlbds.user_service.repository.OwnerRepository;
import com.example.qlbds.viewing_service.repository.ViewingRepository;

@ExtendWith(MockitoExtension.class)
class PropertyServiceImplTest {

    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private PropertyMapper propertyMapper;
    @Mock
    private PropertyImageRepository propertyImageRepository;
    @Mock
    private FileUploadService fileUploadService;
    @Mock
    private OwnerRepository ownerRepository;
    @Mock
    private AgentRepository agentRepository;
    @Mock
    private SlugService slugService;
    @Mock
    private ViewingRepository viewingRepository;
    @Mock
    private ConversationRepository conversationRepository;

    @InjectMocks
    private PropertyServiceImpl propertyService;

    private Property property;
    private PropertyResponse propertyResponse;

    @BeforeEach
    void setUp() {
        property = Property.builder()
                .id(1L)
                .title("Test Property")
                .slug("test-property")
                .price(new BigDecimal("1000000"))
                .area(100.0)
                .bedrooms(2)
                .bathrooms(1)
                .address("123 Test St")
                .city("HCM")
                .district("District 1")
                .status(PropertyStatus.PENDING)
                .visibility(false)
                .viewCount(0)
                .isDeleted(false)
                .build();

        propertyResponse = PropertyResponse.builder()
                .id(1L)
                .title("Test Property")
                .description("Description")
                .price(new BigDecimal("1000000"))
                .area(100.0)
                .bedrooms(2)
                .bathrooms(1)
                .address("123 Test St")
                .city("HCM")
                .district("District 1")
                .slug("test-property")
                .status(PropertyStatus.PENDING)
                .visibility(false)
                .stats(new PropertyResponse.StatsInfo(0, 0))
                .build();
    }

    // --- CREATE ---
    @Test
    void create_ShouldCreateProperty_WhenRequestIsValid() {
        CreatePropertyRequest request = new CreatePropertyRequest(
                "Test Property", "Description", new BigDecimal("1000000"), 100.0, 2, 1,
                "123 Test St", "HCM", "District 1", 1L, null
        );
        Owner owner = new Owner();
        owner.setId(1L);

        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(slugService.toSlug(request.title())).thenReturn("test-property");
        when(propertyRepository.save(any(Property.class))).thenReturn(property);
        when(propertyMapper.toResponse(property)).thenReturn(propertyResponse);

        PropertyResponse response = propertyService.create(request);

        assertNotNull(response);
        assertEquals("Test Property", response.getTitle());
        verify(propertyRepository).save(any(Property.class));
    }

    @Test
    void create_ShouldThrowException_WhenOwnerNotFound() {
        CreatePropertyRequest request = new CreatePropertyRequest(
                "Test Property", "Description", new BigDecimal("1000000"), 100.0, 2, 1,
                "123 Test St", "HCM", "District 1", 99L, null
        );

        when(ownerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> propertyService.create(request));
    }

    @Test
    void create_ShouldThrowException_WhenAgentNotFound() {
        CreatePropertyRequest request = new CreatePropertyRequest(
                "Test Property", "Description", new BigDecimal("1000000"), 100.0, 2, 1,
                "123 Test St", "HCM", "District 1", null, 99L
        );

        when(agentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> propertyService.create(request));
    }

    // --- FIND BY ID ---
    @Test
    void findById_ShouldReturnPropertyAndIncreaseViewCount() {
        when(propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(1L)).thenReturn(Optional.of(property));
        when(propertyRepository.save(property)).thenReturn(property);
        when(propertyMapper.toResponse(property)).thenReturn(propertyResponse);

        PropertyResponse response = propertyService.findById(1L);

        assertNotNull(response);
        assertEquals(1, property.getViewCount()); // View count should be increased
        verify(propertyRepository).save(property);
    }

    @Test
    void findById_ShouldThrowException_WhenPropertyNotFound() {
        when(propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> propertyService.findById(99L));
    }

    // --- FIND BY SLUG ---
    @Test
    void findBySlug_ShouldReturnPropertyAndIncreaseViewCount() {
        when(propertyRepository.findBySlugAndVisibilityTrueAndIsDeletedFalse("test-property")).thenReturn(Optional.of(property));
        when(propertyRepository.save(property)).thenReturn(property);
        when(propertyMapper.toResponse(property)).thenReturn(propertyResponse);

        PropertyResponse response = propertyService.findBySlug("test-property");

        assertNotNull(response);
        assertEquals(1, property.getViewCount());
        verify(propertyRepository).save(property);
    }

    @Test
    void findBySlug_ShouldThrowException_WhenPropertyNotFound() {
        when(propertyRepository.findBySlugAndVisibilityTrueAndIsDeletedFalse("unknown")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> propertyService.findBySlug("unknown"));
    }

    // --- UPDATE ---
    @Test
    void update_ShouldUpdatePropertyAndResetStatusToPending() {
        UpdatePropertyRequest request = new UpdatePropertyRequest(
                "Updated Title", "Updated Desc", new BigDecimal("2000000"), 120.0, 3, 2,
                "456 Test St", "HN", "District 2"
        );

        when(propertyRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(property));
        when(slugService.toSlug("Updated Title")).thenReturn("updated-title");

        propertyService.update(1L, request);

        assertEquals("Updated Title", property.getTitle());
        assertEquals(PropertyStatus.PENDING, property.getStatus());
        assertFalse(property.getVisibility());
        assertNull(property.getRejectionReason());
        verify(propertyRepository, never()).save(any()); // Save is handled by dirty checking in @Transactional
    }

    @Test
    void update_ShouldThrowException_WhenPropertyNotFound() {
        UpdatePropertyRequest request = new UpdatePropertyRequest(
                "Title", "Desc", new BigDecimal("1"), 1.0, 1, 1, "St", "City", "District"
        );
        when(propertyRepository.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> propertyService.update(99L, request));
    }

    // --- DELETE ---
    @Test
    void delete_ShouldSoftDeleteProperty() {
        when(propertyRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(property));

        propertyService.delete(1L);

        assertTrue(property.getIsDeleted());
        assertEquals(PropertyStatus.DELETED, property.getStatus());
        assertFalse(property.getVisibility());
    }

    @Test
    void delete_ShouldThrowException_WhenPropertyNotFound() {
        when(propertyRepository.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> propertyService.delete(99L));
    }

    // --- UPLOAD IMAGES ---
    @Test
    void uploadImages_ShouldUploadAndSaveImages() throws IOException {
        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(file1, file2);

        when(propertyRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(property));
        when(fileUploadService.uploadMultipleFiles(files, "properties/1")).thenReturn(List.of("url1", "url2"));
        when(propertyImageRepository.findByPropertyId(1L)).thenReturn(List.of());

        propertyService.uploadImages(1L, files);

        verify(propertyImageRepository).saveAll(anyList());
    }

    @Test
    void uploadImages_ShouldThrowException_WhenUploadFails() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(propertyRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(property));
        when(fileUploadService.uploadMultipleFiles(List.of(file), "properties/1")).thenThrow(new IOException("Upload failed"));

        assertThrows(RuntimeException.class, () -> propertyService.uploadImages(1L, List.of(file)));
    }

    // --- UPDATE IMAGE ---
    @Test
    void updateImage_ShouldUpdateImageSuccessfully() throws IOException {
        MultipartFile newFile = mock(MultipartFile.class);
        PropertyImage image = new PropertyImage();
        image.setId(10L);
        image.setProperty(property);
        image.setImageUrl("old-url");


        when(propertyImageRepository.findById(10L)).thenReturn(Optional.of(image));
        when(fileUploadService.uploadFile(newFile, "properties/1")).thenReturn("new-url");

        propertyService.updateImage(1L, 10L, newFile);

        assertEquals("new-url", image.getImageUrl());
        verify(fileUploadService).deleteFile("old-url");
    }

    @Test
    void updateImage_ShouldThrowException_WhenImageNotFound() {
        MultipartFile newFile = mock(MultipartFile.class);

        when(propertyImageRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> propertyService.updateImage(1L, 10L, newFile));
    }

    @Test
    void updateImage_ShouldThrowException_WhenImageDoesNotBelongToProperty() {
        MultipartFile newFile = mock(MultipartFile.class);
        PropertyImage image = new PropertyImage();
        image.setId(10L);
        Property otherProperty = new Property();
        otherProperty.setId(99L);
        image.setProperty(otherProperty);


        when(propertyImageRepository.findById(10L)).thenReturn(Optional.of(image));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
                () -> propertyService.updateImage(1L, 10L, newFile));
        assertTrue(ex.getMessage().contains("không thuộc về bất động sản này"));
    }

    // --- DELETE IMAGE ---
    @Test
    void deleteImage_ShouldDeleteImageSuccessfully() throws IOException {
        PropertyImage image = new PropertyImage();
        image.setId(10L);
        image.setProperty(property);
        image.setImageUrl("url-to-delete");


        when(propertyImageRepository.findById(10L)).thenReturn(Optional.of(image));

        propertyService.deleteImage(1L, 10L);

        verify(fileUploadService).deleteFile("url-to-delete");
        verify(propertyImageRepository).delete(image);
    }

    @Test
    void deleteImage_ShouldThrowException_WhenImageNotFound() {

        when(propertyImageRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> propertyService.deleteImage(1L, 10L));
    }

    @Test
    void deleteImage_ShouldThrowException_WhenImageDoesNotBelongToProperty() {
        PropertyImage image = new PropertyImage();
        image.setId(10L);
        Property otherProperty = new Property();
        otherProperty.setId(99L);
        image.setProperty(otherProperty);


        when(propertyImageRepository.findById(10L)).thenReturn(Optional.of(image));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
                () -> propertyService.deleteImage(1L, 10L));
        assertTrue(ex.getMessage().contains("không thuộc về bất động sản này"));
    }

    // --- CHANGE STATUS ---
    @Test
    void changeStatus_ShouldApprovePropertyAndSetVisibilityTrue() {
        when(propertyRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(property));
        when(propertyMapper.toResponse(property)).thenReturn(propertyResponse);

        propertyService.changeStatus(1L, PropertyStatus.APPROVED, null);

        assertEquals(PropertyStatus.APPROVED, property.getStatus());
        assertTrue(property.getVisibility());
        assertNull(property.getRejectionReason());
    }

    @Test
    void changeStatus_ShouldRejectPropertyAndSetVisibilityFalse() {
        when(propertyRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(property));
        when(propertyMapper.toResponse(property)).thenReturn(propertyResponse);

        propertyService.changeStatus(1L, PropertyStatus.REJECTED, "Invalid info");

        assertEquals(PropertyStatus.REJECTED, property.getStatus());
        assertFalse(property.getVisibility());
        assertEquals("Invalid info", property.getRejectionReason());
    }

    @Test
    void changeStatus_ShouldThrowException_WhenPropertyNotFound() {
        when(propertyRepository.findByIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, 
                () -> propertyService.changeStatus(99L, PropertyStatus.APPROVED, null));
    }

    // --- GET ANALYTICS ---
    @Test
    void getAnalytics_ShouldReturnAnalyticsData() {
        property.setViewCount(100);
        when(propertyRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(property));
        when(viewingRepository.countByPropertyIdAndIsDeletedFalse(1L)).thenReturn(5);
        when(conversationRepository.countByPropertyId(1L)).thenReturn(10);

        PropertyAnalyticsResponse analytics = propertyService.getAnalytics(1L);

        assertEquals(1L, analytics.getPropertyId());
        assertEquals(100, analytics.getViewCount());
        assertEquals(5, analytics.getViewingAppointments());
        assertEquals(10, analytics.getTotalConversations());
        assertEquals(5.0, analytics.getConversionRate()); // (5 / 100) * 100
    }

    @Test
    void getAnalytics_ShouldReturnZeroConversionRate_WhenViewCountIsZero() {
        property.setViewCount(0);
        when(propertyRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(property));
        when(viewingRepository.countByPropertyIdAndIsDeletedFalse(1L)).thenReturn(5);
        when(conversationRepository.countByPropertyId(1L)).thenReturn(10);

        PropertyAnalyticsResponse analytics = propertyService.getAnalytics(1L);

        assertEquals(0.0, analytics.getConversionRate());
    }
}
