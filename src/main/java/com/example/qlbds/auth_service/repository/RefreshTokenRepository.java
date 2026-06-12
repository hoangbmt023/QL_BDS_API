package com.example.qlbds.auth_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.qlbds.auth_service.entity.RefreshToken;
import com.example.qlbds.user_service.entity.User;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    int deleteByUser(User user);

    long countByUser(User user);

    @Query(value = "SELECT * FROM refresh_tokens WHERE user_id = ?1 ORDER BY created_at ASC LIMIT 1", nativeQuery = true)
    Optional<RefreshToken> findOldestByUserId(Long userId);
}
