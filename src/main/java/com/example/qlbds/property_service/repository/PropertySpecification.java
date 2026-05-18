package com.example.qlbds.property_service.repository;

import org.springframework.data.jpa.domain.Specification;
import com.example.qlbds.property_service.entity.Property;
import com.example.qlbds.shared.entity.enums.PropertyStatus;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PropertySpecification {
    public static Specification<Property> filterProperties(
            String search,
            String city,
            String district,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer bedrooms,
            Integer bathrooms,
            PropertyStatus status) {
        
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Only visible and not deleted properties
            predicates.add(criteriaBuilder.isTrue(root.get("visibility")));
            predicates.add(criteriaBuilder.isFalse(root.get("isDeleted")));
            
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = "%" + search.trim().toLowerCase() + "%";
                Predicate titleLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchLower);
                Predicate addressLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), searchLower);
                predicates.add(criteriaBuilder.or(titleLike, addressLike));
            }
            
            if (city != null && !city.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("city")), city.trim().toLowerCase()));
            }
            
            if (district != null && !district.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("district")), district.trim().toLowerCase()));
            }
            
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            
            if (bedrooms != null) {
                predicates.add(criteriaBuilder.equal(root.get("bedrooms"), bedrooms));
            }
            
            if (bathrooms != null) {
                predicates.add(criteriaBuilder.equal(root.get("bathrooms"), bathrooms));
            }
            
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
