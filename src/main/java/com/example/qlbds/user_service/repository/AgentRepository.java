package com.example.qlbds.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.qlbds.user_service.entity.Agent;
import com.example.qlbds.user_service.entity.User;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {

    Optional<Agent> findByUser(User user);

    boolean existsByUser(User user);
}
