package com.example.qlbds.property_service.service.impl;

import com.example.qlbds.common.exception.ResourceNotFoundException;
import com.example.qlbds.common.response.PageResponse;
import com.example.qlbds.property_service.dto.CreatePropertyRequest;
import com.example.qlbds.property_service.dto.PropertyAnalyticsResponse;
import com.example.qlbds.property_service.dto.PropertyResponse;
import com.example.qlbds.property_service.dto.UpdatePropertyRequest;
import com.example.qlbds.property_service.entity.Property;
import com.example.qlbds.property_service.entity.PropertyImage;
import com.example.qlbds.property_service.mapper.PropertyMapper;
import com.example.qlbds.property_service.repository.PropertyImageRepository;
import com.example.qlbds.property_service.repository.PropertyRepository;
import com.example.qlbds.property_service.repository.PropertySpecification;
import com.example.qlbds.property_service.service.PropertyService;
import com.example.qlbds.shared.entity.enums.PropertyStatus;
import com.example.qlbds.shared.service.FileUploadService;
import com.example.qlbds.user_service.entity.Agent;
import com.example.qlbds.user_service.entity.Owner;
import com.example.qlbds.user_service.repository.AgentRepository;
import com.example.qlbds.user_service.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private static final Logger log = LoggerFactory.getLogger(PropertyServiceImpl.class);

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;
    private final PropertyImageRepository propertyImageRepository;
    private final FileUploadService fileUploadService;
    private final OwnerRepository ownerRepository;
    private final AgentRepository agentRepository;

    // Tạo bất động sản mới — mặc định PENDING, chờ Moderator duyệt
    @Override
    @Transactional
    public PropertyResponse create(CreatePropertyRequest request) {
        log.info("[Create Property] title={}", request.title());

        Owner owner = null;
        if (request.ownerId() != null) {
            owner = ownerRepository.findById(request.ownerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Owner", request.ownerId()));
        }

        Agent agent = null;
        if (request.agentId() != null) {
            agent = agentRepository.findById(request.agentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Agent", request.agentId()));
        }

        Property property = Property.builder()
                .title(request.title() != null ? request.title().strip() : null)
                .description(request.description())
                .price(request.price())
                .area(request.area())
                .bedrooms(request.bedrooms())
                .bathrooms(request.bathrooms())
                .address(request.address())
                .city(request.city())
                .district(request.district())
                .owner(owner)
                .agent(agent)
                .status(PropertyStatus.PENDING)   // mặc định PENDING
                .visibility(false)                // ẩn cho đến khi được duyệt
                .isDeleted(false)
                .build();

        return propertyMapper.toResponse(propertyRepository.save(property));
    }

    // Lấy danh sách bất động sản có phân trang và lọc động
    @Override
    public PageResponse<PropertyResponse> findAll(
            String search, String city, String district,
            BigDecimal minPrice, BigDecimal maxPrice,
            Integer bedrooms, Integer bathrooms,
            PropertyStatus status, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Specification<Property> spec = PropertySpecification.filterProperties(
                search, city, district, minPrice, maxPrice, bedrooms, bathrooms, status);

        return PageResponse.from(
                propertyRepository.findAll(spec, pageable).map(propertyMapper::toResponse));
    }

    // Lấy chi tiết một bất động sản (chỉ hiển thị bài đã duyệt và chưa xóa)
    @Override
    public PropertyResponse findById(Long id) {
        return propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(id)
                .map(propertyMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));
    }

    // Cập nhật thông tin — tự động reset về PENDING để duyệt lại
    @Override
    @Transactional
    public PropertyResponse update(Long id, UpdatePropertyRequest request) {
        Property property = propertyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));

        property.setTitle(request.title() != null ? request.title().strip() : null);
        property.setDescription(request.description());
        property.setPrice(request.price());
        property.setArea(request.area());
        property.setBedrooms(request.bedrooms());
        property.setBathrooms(request.bathrooms());
        property.setAddress(request.address());
        property.setCity(request.city());
        property.setDistrict(request.district());
        property.setStatus(PropertyStatus.PENDING);  // yêu cầu duyệt lại
        property.setVisibility(false);               // ẩn trong lúc chờ duyệt
        property.setRejectionReason(null);           // xóa lý do từ chối cũ

        return propertyMapper.toResponse(property);
    }

    // Xóa mềm bất động sản — không hard-delete, chỉ đánh dấu isDeleted
    @Override
    @Transactional
    public void delete(Long id) {
        Property property = propertyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));
        property.setIsDeleted(true);
        property.setStatus(PropertyStatus.DELETED);
        property.setVisibility(false);
        log.info("[Delete Property] id={} - Đã xóa mềm", id);
    }

    // Tìm thực thể Property (dùng nội bộ)
    @Override
    public Property findPropertyById(Long id) {
        return propertyRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));
    }

    // Upload nhiều ảnh cho một property lên Cloudinary
    @Override
    @Transactional
    public List<String> uploadImages(Long propertyId, List<MultipartFile> files) {
        Property property = findPropertyById(propertyId);

        List<String> imageUrls;
        try {
            imageUrls = fileUploadService.uploadMultipleFiles(files, "properties/" + propertyId);
        } catch (IOException e) {
            log.error("[Upload Images] property={} error={}", propertyId, e.getMessage());
            throw new RuntimeException("Không thể upload ảnh: " + e.getMessage());
        }

        boolean hasExistingImages = !propertyImageRepository.findByPropertyId(propertyId).isEmpty();
        List<PropertyImage> images = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            images.add(PropertyImage.builder()
                    .property(property)
                    .imageUrl(imageUrls.get(i))
                    .isMain(!hasExistingImages && i == 0) // ảnh đầu tiên là ảnh đại diện nếu chưa có
                    .build());
        }
        propertyImageRepository.saveAll(images);

        return imageUrls;
    }

    // Gợi ý property tương tự (cùng city & district)
    @Override
    public List<PropertyResponse> getSimilarProperties(Long id, int limit) {
        Property property = findPropertyById(id);
        Pageable pageable = PageRequest.of(0, limit);
        return propertyRepository
                .findSimilarProperties(property.getCity(), property.getDistrict(), id, pageable)
                .map(propertyMapper::toResponse)
                .getContent();
    }

    // Đổi trạng thái (dùng cho Moderator/Admin: duyệt, từ chối, ẩn bài...)
    @Override
    @Transactional
    public PropertyResponse changeStatus(Long id, PropertyStatus newStatus, String reason) {
        Property property = findPropertyById(id);
        property.setStatus(newStatus);

        if (reason != null && !reason.isBlank()) {
            property.setRejectionReason(reason.strip());
        }

        // Đồng bộ visibility theo trạng thái
        if (newStatus == PropertyStatus.APPROVED) {
            property.setVisibility(true);
            property.setRejectionReason(null); // xóa lý do cũ khi được duyệt
        } else if (newStatus == PropertyStatus.HIDDEN
                || newStatus == PropertyStatus.REJECTED
                || newStatus == PropertyStatus.DELETED
                || newStatus == PropertyStatus.SOLD
                || newStatus == PropertyStatus.RENTED) {
            property.setVisibility(false);
        }

        return propertyMapper.toResponse(property);
    }

    // Thống kê hiệu quả listing
    @Override
    public PropertyAnalyticsResponse getAnalytics(Long id) {
        Property property = findPropertyById(id);

        // TODO: thay mock bằng query thực khi có ViewingService & ConversationService
        int viewings = 0;
        int convos = 0;
        double conversionRate = 0.0;
        if (property.getViewCount() != null && property.getViewCount() > 0) {
            conversionRate = ((double) viewings / property.getViewCount()) * 100.0;
        }

        return PropertyAnalyticsResponse.builder()
                .propertyId(property.getId())
                .viewCount(property.getViewCount())
                .favoriteCount(property.getFavoriteCount())
                .viewingAppointments(viewings)
                .totalConversations(convos)
                .conversionRate(conversionRate)
                .build();
    }
}
