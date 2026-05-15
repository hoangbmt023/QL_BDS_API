package com.example.qlbds.user_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.qlbds.shared.entity.enums.AgentRequestStatus;
import com.example.qlbds.user_service.entity.AgentRequest;
import com.example.qlbds.user_service.entity.User;

@Repository
public interface AgentRequestRepository extends JpaRepository<AgentRequest, Long> {

    // Kiểm tra user đã có request PENDING chưa (chỉ lấy record chưa xóa mềm)
    boolean existsByUserAndStatusAndIsDeletedFalse(User user, AgentRequestStatus status);

    // Lấy tất cả request theo trạng thái (Admin) - không lấy đã xóa mềm
    List<AgentRequest> findAllByStatusAndIsDeletedFalse(AgentRequestStatus status);

    // Lấy request mới nhất của user (chưa xóa mềm)
    Optional<AgentRequest> findTopByUserAndIsDeletedFalseOrderByCreatedAtDesc(User user);
}
