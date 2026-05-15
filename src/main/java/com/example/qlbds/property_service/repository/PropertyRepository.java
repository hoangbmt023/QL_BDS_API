package com.example.qlbds.property_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.qlbds.property_service.entity.Property;
import com.example.qlbds.shared.entity.enums.PropertyStatus;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    // Lấy danh sách property theo status và chưa bị xóa, đang hiển thị
    Page<Property> findByStatusAndVisibilityTrueAndIsDeletedFalse(PropertyStatus status, Pageable pageable);

    // Lấy tất cả property đang hiển thị và chưa bị xóa
    Page<Property> findByVisibilityTrueAndIsDeletedFalse(Pageable pageable);

    // Tìm kiếm theo tiêu đề và chưa bị xóa, đang hiển thị
    Page<Property> findByTitleContainingIgnoreCaseAndVisibilityTrueAndIsDeletedFalse(String title, Pageable pageable);

    // Tìm chi tiết theo ID và chưa bị xóa, đang hiển thị
    java.util.Optional<Property> findByIdAndVisibilityTrueAndIsDeletedFalse(Long id);

    // Tìm theo ID bất kể visibility nhưng phải chưa bị xóa
    java.util.Optional<Property> findByIdAndIsDeletedFalse(Long id);
}
