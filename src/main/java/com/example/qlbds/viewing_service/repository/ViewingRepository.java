package com.example.qlbds.viewing_service.repository;

import com.example.qlbds.shared.entity.enums.ViewingStatus;
import com.example.qlbds.user_service.entity.User;
import com.example.qlbds.viewing_service.entity.Viewing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ViewingRepository extends JpaRepository<Viewing, Long> {
    
    Page<Viewing> findByUserAndIsDeletedFalse(User user, Pageable pageable);
    
    Optional<Viewing> findByIdAndIsDeletedFalse(Long id);
    
    @Query("SELECT COUNT(v) > 0 FROM Viewing v WHERE v.property.id = :propertyId AND v.isDeleted = false " +
           "AND v.status IN (:statuses) AND " +
           "((v.scheduledTime BETWEEN :startTime AND :endTime)) " +
           "AND (:excludeId IS NULL OR v.id != :excludeId)")
    boolean existsConflictingViewing(
            @Param("propertyId") Long propertyId, 
            @Param("startTime") LocalDateTime startTime, 
            @Param("endTime") LocalDateTime endTime,
            @Param("statuses") List<ViewingStatus> statuses,
            @Param("excludeId") Long excludeId);

    @Query("SELECT v FROM Viewing v WHERE v.user = :user AND v.isDeleted = false " +
           "AND (:status IS NULL OR v.status = :status) " +
           "AND (:upcoming = false OR v.scheduledTime >= :now)")
    Page<Viewing> findMyViewingsFiltered(@Param("user") User user, 
                                         @Param("status") ViewingStatus status, 
                                         @Param("upcoming") boolean upcoming, 
                                         @Param("now") LocalDateTime now,
                                         Pageable pageable);

    @Query("SELECT v FROM Viewing v WHERE v.isDeleted = false AND " +
           "(v.property.owner.user = :user OR v.property.agent.user = :user) " +
           "AND (:status IS NULL OR v.status = :status) " +
           "AND (:upcoming = false OR v.scheduledTime >= :now)")
    Page<Viewing> findManagedViewingsFiltered(@Param("user") User user, 
                                              @Param("status") ViewingStatus status, 
                                              @Param("upcoming") boolean upcoming, 
                                              @Param("now") LocalDateTime now,
                                              Pageable pageable);

    @Query("SELECT v.status, COUNT(v) FROM Viewing v WHERE v.isDeleted = false AND " +
           "(v.property.owner.user = :user OR v.property.agent.user = :user) GROUP BY v.status")
    List<Object[]> countManagedViewingsByStatus(@Param("user") User user);
    
    @Query("SELECT v.status, COUNT(v) FROM Viewing v WHERE v.isDeleted = false AND v.user = :user GROUP BY v.status")
    List<Object[]> countMyViewingsByStatus(@Param("user") User user);
    
    List<Viewing> findByPropertyIdAndScheduledTimeBetweenAndStatusInAndIsDeletedFalse(
            Long propertyId, LocalDateTime start, LocalDateTime end, List<ViewingStatus> statuses);
}
