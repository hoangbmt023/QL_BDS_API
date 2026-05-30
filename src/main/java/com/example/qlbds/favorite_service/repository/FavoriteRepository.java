package com.example.qlbds.favorite_service.repository;

import com.example.qlbds.favorite_service.entity.Favorite;
import com.example.qlbds.property_service.entity.Property;
import com.example.qlbds.user_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
    Optional<Favorite> findByUserAndProperty(User user, Property property);
    
    boolean existsByUserAndProperty(User user, Property property);
    
    Page<Favorite> findByUser(User user, Pageable pageable);

    List<Favorite> findAllByUser(User user);

    List<Favorite> findByUserAndPropertyIdIn(User user, List<Long> propertyIds);
}
