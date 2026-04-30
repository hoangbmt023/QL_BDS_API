package com.example.qlbds.property_service.service;

import com.example.qlbds.common.response.PageResponse;
import com.example.qlbds.property_service.dto.CreatePropertyRequest;
import com.example.qlbds.property_service.dto.PropertyResponse;
import com.example.qlbds.property_service.dto.UpdatePropertyRequest;
import com.example.qlbds.property_service.entity.Property;

public interface PropertyService {

    PropertyResponse create(CreatePropertyRequest request);

    PageResponse<PropertyResponse> findAll(String search, int page, int size);

    PropertyResponse findById(Long id);

    PropertyResponse update(Long id, UpdatePropertyRequest request);

    void delete(Long id);

    Property findPropertyById(Long id);
}
