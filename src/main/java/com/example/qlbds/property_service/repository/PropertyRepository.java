package com.example.qlbds.property_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.qlbds.property_service.entity.Property;
import com.example.qlbds.shared.entity.enums.PropertyStatus;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    Page<Property> findByStatusAndVisibilityTrue(PropertyStatus status, Pageable pageable);

    Page<Property> findByVisibilityTrue(Pageable pageable);

    Page<Property> findByTitleContainingIgnoreCaseAndVisibilityTrue(String title, Pageable pageable);

    java.util.Optional<Property> findByIdAndVisibilityTrue(Long id);
}
