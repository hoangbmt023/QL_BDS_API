package com.example.qlbds.property_service.controller;

import com.example.qlbds.common.response.PageResponse;
import com.example.qlbds.property_service.dto.CreatePropertyRequest;
import com.example.qlbds.property_service.dto.PropertyResponse;
import com.example.qlbds.property_service.dto.UpdatePropertyRequest;
import com.example.qlbds.property_service.service.PropertyService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
@Validated
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyResponse create(@Valid @RequestBody CreatePropertyRequest request) {
        return propertyService.create(request);
    }

    @GetMapping
    public PageResponse<PropertyResponse> findAll(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "page", defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(value = 1, message = "size must be >= 1") int size) {

        return propertyService.findAll(search, page, size);
    }

    @GetMapping("/{id}")
    public PropertyResponse findById(@PathVariable(name = "id") Long id) {
        return propertyService.findById(id);
    }

    @PatchMapping("/{id}")
    public PropertyResponse update(
            @PathVariable(name = "id") Long id,
            @Valid @RequestBody UpdatePropertyRequest request) {
        return propertyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable(name = "id") Long id) {
        propertyService.delete(id);
    }
}
