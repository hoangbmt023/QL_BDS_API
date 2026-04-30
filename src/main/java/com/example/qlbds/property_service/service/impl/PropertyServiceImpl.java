package com.example.qlbds.property_service.service.impl;

import com.example.qlbds.common.exception.ResourceNotFoundException;
import com.example.qlbds.common.response.PageResponse;
import com.example.qlbds.property_service.dto.CreatePropertyRequest;
import com.example.qlbds.property_service.dto.PropertyResponse;
import com.example.qlbds.property_service.dto.UpdatePropertyRequest;
import com.example.qlbds.property_service.entity.Property;
import com.example.qlbds.property_service.mapper.PropertyMapper;
import com.example.qlbds.property_service.repository.PropertyRepository;
import com.example.qlbds.property_service.service.PropertyService;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private static final Logger log = LoggerFactory.getLogger(PropertyServiceImpl.class);

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;

    @Override
    @Transactional
    public PropertyResponse create(CreatePropertyRequest request) {
        log.info("[Create Property] title={}", request.title());

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
                .build();

        return propertyMapper.toResponse(propertyRepository.save(property));
    }

    @Override
    public PageResponse<PropertyResponse> findAll(String search, int page, int size) {
        String term = (search != null && !search.isBlank()) ? search.strip() : null;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        
        if (term != null) {
            return PageResponse.from(
                    propertyRepository.findByTitleContainingIgnoreCaseAndVisibilityTrue(term, pageable)
                            .map(propertyMapper::toResponse));
        } else {
            return PageResponse.from(
                    propertyRepository.findByVisibilityTrue(pageable)
                            .map(propertyMapper::toResponse));
        }
    }

    @Override
    public PropertyResponse findById(Long id) {
        return propertyRepository.findByIdAndVisibilityTrue(id)
                .map(propertyMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));
    }

    @Override
    @Transactional
    public PropertyResponse update(Long id, UpdatePropertyRequest request) {
        Property property = propertyRepository.findByIdAndVisibilityTrue(id)
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

        return propertyMapper.toResponse(property);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Property property = propertyRepository.findByIdAndVisibilityTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));
        property.setVisibility(false);
    }

    @Override
    public Property findPropertyById(Long id) {
        return propertyRepository.findByIdAndVisibilityTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));
    }
}
