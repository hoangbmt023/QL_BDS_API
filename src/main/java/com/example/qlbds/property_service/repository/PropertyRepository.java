package com.example.qlbds.property_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.qlbds.property_service.entity.Property;
import com.example.qlbds.shared.entity.enums.PropertyStatus;
import com.example.qlbds.user_service.entity.Agent;
import com.example.qlbds.user_service.entity.Owner;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long>, JpaSpecificationExecutor<Property> {

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

    // Gợi ý property tương tự (cùng district, city, giới hạn số lượng)
    @Query("SELECT p FROM Property p WHERE p.city = :city AND p.district = :district AND p.id != :id AND p.visibility = true AND p.isDeleted = false")
    Page<Property> findSimilarProperties(@Param("city") String city, @Param("district") String district, @Param("id") Long id, Pageable pageable);
}
