package com.example.qlbds.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.qlbds.user_service.entity.Owner;
import com.example.qlbds.user_service.entity.User;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    Optional<Owner> findByUser(User user);

    boolean existsByUser(User user);
}
