package com.example.qlbds.property_service.dto;

import java.math.BigDecimal;

public record UpdatePropertyRequest(
        String title,
        String description,
        BigDecimal price,
        Double area,
        Integer bedrooms,
        Integer bathrooms,
        String address,
        String city,
        String district
) {}
