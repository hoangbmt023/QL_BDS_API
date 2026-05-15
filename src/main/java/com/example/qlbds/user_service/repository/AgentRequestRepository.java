package com.example.qlbds.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.qlbds.user_service.entity.AgentRequest;
import com.example.qlbds.user_service.entity.User;
import com.example.qlbds.shared.entity.enums.AgentRequestStatus;

import java.util.List;

@Repository
public interface AgentRequestRepository extends JpaRepository<AgentRequest, Long> {

    // Kiểm tra user đã có request đang chờ duyệt chưa
    boolean existsByUserAndStatus(User user, AgentRequestStatus status);

    // Lấy tất cả request theo trạng thái (dành cho Admin)
    List<AgentRequest> findAllByStatus(AgentRequestStatus status);

    // Lấy request mới nhất của user
    Optional<AgentRequest> findTopByUserOrderByCreatedAtDesc(User user);
}
