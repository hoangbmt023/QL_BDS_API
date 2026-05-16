package com.example.qlbds.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.qlbds.user_service.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm người dùng theo tên đăng nhập (chưa bị xóa)
    Optional<User> findByUsernameAndIsDeletedFalse(String username);

    // Tìm người dùng theo email (chưa bị xóa)
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    // Kiểm tra tên đăng nhập đã tồn tại chưa (kể cả đã xóa để tránh trùng lặp username cũ nếu cần, hoặc chỉ active)
    // Thường thì check active để cho phép đăng ký lại nếu đã xóa
    boolean existsByUsernameAndIsDeletedFalse(String username);

    // Kiểm tra email đã tồn tại chưa
    boolean existsByEmailAndIsDeletedFalse(String email);

    // Tìm theo username bất kể trạng thái (dùng cho nội bộ hoặc restore)
    Optional<User> findByUsername(String username);

    // Tìm theo email bất kể trạng thái
    Optional<User> findByEmail(String email);

    // Kiểm tra tên đăng nhập (bất kể trạng thái)
    boolean existsByUsername(String username);

    // Kiểm tra email (bất kể trạng thái)
    boolean existsByEmail(String email);
}
