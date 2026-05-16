package com.example.qlbds.property_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.qlbds.property_service.entity.Property;
import com.example.qlbds.shared.entity.enums.PropertyStatus;
import com.example.qlbds.user_service.entity.Agent;
import com.example.qlbds.user_service.entity.Owner;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    // Lấy danh sách property theo status và chưa bị xóa, đang hiển thị
    Page<Property> findByStatusAndVisibilityTrueAndIsDeletedFalse(PropertyStatus status, Pageable pageable);

    // Lấy tất cả property đang hiển thị và chưa bị xóa
    Page<Property> findByVisibilityTrueAndIsDeletedFalse(Pageable pageable);

    // Tìm kiếm theo tiêu đề và chưa bị xóa, đang hiển thị
    Page<Property> findByTitleContainingIgnoreCaseAndVisibilityTrueAndIsDeletedFalse(String title, Pageable pageable);

    // Tìm chi tiết theo ID và chưa bị xóa, đang hiển thị
    Optional<Property> findByIdAndVisibilityTrueAndIsDeletedFalse(Long id);

    // Tìm theo ID bất kể visibility nhưng phải chưa bị xóa
    Optional<Property> findByIdAndIsDeletedFalse(Long id);

    // Lấy tất cả property của một Owner (bao gồm cả đã xóa để khôi phục)
    List<Property> findAllByOwner(Owner owner);

    // Lấy tất cả property của một Agent (bao gồm cả đã xóa để khôi phục)
    List<Property> findAllByAgent(Agent agent);
}
