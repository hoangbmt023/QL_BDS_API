package com.example.qlbds.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.qlbds.user_service.entity.Agent;
import com.example.qlbds.user_service.entity.User;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {

    // Tìm Agent đang active (chưa bị xóa mềm)
    Optional<Agent> findByUserAndIsDeletedFalse(User user);

    // Tìm Agent kể cả đã xóa mềm (dùng khi restore)
    Optional<Agent> findByUser(User user);

    // Kiểm tra user đã có Agent record đang active chưa
    boolean existsByUserAndIsDeletedFalse(User user);
}
