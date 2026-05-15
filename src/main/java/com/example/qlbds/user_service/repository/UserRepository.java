package com.example.qlbds.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.qlbds.user_service.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm người dùng theo tên đăng nhập
    Optional<User> findByUsername(String username);

    // Tìm người dùng theo email
    Optional<User> findByEmail(String email);

    // Kiểm tra tên đăng nhập đã tồn tại chưa
    boolean existsByUsername(String username);

    // Kiểm tra email đã tồn tại chưa
    boolean existsByEmail(String email);
}
