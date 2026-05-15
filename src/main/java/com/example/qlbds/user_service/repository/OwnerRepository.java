package com.example.qlbds.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.qlbds.user_service.entity.Owner;
import com.example.qlbds.user_service.entity.User;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    // Tìm Owner đang active (chưa bị xóa mềm)
    Optional<Owner> findByUserAndIsDeletedFalse(User user);

    // Tìm Owner kể cả đã xóa mềm (dùng khi restore)
    Optional<Owner> findByUser(User user);

    // Kiểm tra user đã có Owner record đang active chưa
    boolean existsByUserAndIsDeletedFalse(User user);
}
