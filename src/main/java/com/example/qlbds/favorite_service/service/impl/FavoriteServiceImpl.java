package com.example.qlbds.favorite_service.service.impl;

import com.example.qlbds.common.exception.DuplicateResourceException;
import com.example.qlbds.common.exception.ResourceNotFoundException;
import com.example.qlbds.common.response.PageResponse;
import com.example.qlbds.config.CurrentUserService;
import com.example.qlbds.favorite_service.entity.Favorite;
import com.example.qlbds.favorite_service.repository.FavoriteRepository;
import com.example.qlbds.favorite_service.service.FavoriteService;
import com.example.qlbds.property_service.entity.Property;
import com.example.qlbds.property_service.repository.PropertyRepository;
import com.example.qlbds.user_service.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.example.qlbds.favorite_service.dto.DeleteMultipleFavoritesRequest;
import com.example.qlbds.favorite_service.dto.FavoriteActionResponse;
import com.example.qlbds.favorite_service.dto.FavoriteRequest;
import com.example.qlbds.favorite_service.dto.FavoriteResponse;
import com.example.qlbds.favorite_service.mapper.FavoriteMapper;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PropertyRepository propertyRepository;
    private final CurrentUserService currentUserService;
    private final FavoriteMapper favoriteMapper;

    @Override
    @Transactional
    public FavoriteActionResponse addFavorite(FavoriteRequest request) {
        Long propertyId = request.getPropertyId();
        User currentUser = currentUserService.getCurrentUser();

        Property property = propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bất động sản với ID: " + propertyId));

        if (favoriteRepository.existsByUserAndProperty(currentUser, property)) {
            throw new DuplicateResourceException("Bất động sản này đã có trong danh sách yêu thích");
        }

        Favorite favorite = Favorite.builder()
                .user(currentUser)
                .property(property)
                .build();

        favoriteRepository.save(favorite);

        // Cập nhật số lượng yêu thích
        if (property.getFavoriteCount() == null) {
            property.setFavoriteCount(0);
        }
        property.setFavoriteCount(property.getFavoriteCount() + 1);
        propertyRepository.save(property);

        return FavoriteActionResponse.builder()
                .propertyId(propertyId)
                .isFavorite(true)
                .favoriteCount(property.getFavoriteCount())
                .build();
    }

    @Override
    @Transactional
    public FavoriteActionResponse removeFavorite(Long propertyId) {
        User currentUser = currentUserService.getCurrentUser();

        Property property = propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bất động sản với ID: " + propertyId));

        Favorite favorite = favoriteRepository.findByUserAndProperty(currentUser, property)
                .orElseThrow(() -> new ResourceNotFoundException("Bất động sản chưa có trong danh sách yêu thích"));

        favoriteRepository.delete(favorite);

        // Cập nhật số lượng yêu thích
        if (property.getFavoriteCount() != null && property.getFavoriteCount() > 0) {
            property.setFavoriteCount(property.getFavoriteCount() - 1);
            propertyRepository.save(property);
        }

        return FavoriteActionResponse.builder()
                .propertyId(propertyId)
                .isFavorite(false)
                .favoriteCount(property.getFavoriteCount())
                .build();
    }

    @Override
    @Transactional
    public void removeMultipleFavorites(DeleteMultipleFavoritesRequest request) {
        List<Long> propertyIds = request.getPropertyIds();
        if (propertyIds == null || propertyIds.isEmpty())
            return;

        User currentUser = currentUserService.getCurrentUser();
        List<Favorite> favorites = favoriteRepository.findByUserAndPropertyIdIn(currentUser, propertyIds);

        if (favorites.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy bất động sản nào trong danh sách yêu thích để xóa");
        }
        
        if (favorites.size() != propertyIds.size()) {
            throw new ResourceNotFoundException("Một hoặc nhiều bất động sản không có trong danh sách yêu thích");
        }

        for (Favorite favorite : favorites) {
            Property property = favorite.getProperty();
            if (property.getFavoriteCount() != null && property.getFavoriteCount() > 0) {
                property.setFavoriteCount(property.getFavoriteCount() - 1);
                propertyRepository.save(property);
            }
        }

        favoriteRepository.deleteAll(favorites);
    }

    @Override
    @Transactional
    public void removeAllFavorites() {
        User currentUser = currentUserService.getCurrentUser();
        List<Favorite> favorites = favoriteRepository.findAllByUser(currentUser);

        if (favorites.isEmpty()) {
            throw new ResourceNotFoundException("Danh sách yêu thích đang trống, không có gì để xóa");
        }

        for (Favorite favorite : favorites) {
            Property property = favorite.getProperty();
            if (property.getFavoriteCount() != null && property.getFavoriteCount() > 0) {
                property.setFavoriteCount(property.getFavoriteCount() - 1);
                propertyRepository.save(property);
            }
        }

        favoriteRepository.deleteAll(favorites);
    }

    @Override
    @Transactional(readOnly = true)
    public FavoriteActionResponse checkFavorite(Long propertyId) {
        User currentUser = currentUserService.getCurrentUser();

        Property property = propertyRepository.findByIdAndVisibilityTrueAndIsDeletedFalse(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bất động sản với ID: " + propertyId));

        boolean isFavorite = favoriteRepository.existsByUserAndProperty(currentUser, property);

        return FavoriteActionResponse.builder()
                .propertyId(propertyId)
                .isFavorite(isFavorite)
                .favoriteCount(property.getFavoriteCount())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FavoriteResponse> getMyFavorites(int page, int size) {
        User currentUser = currentUserService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Favorite> favoritePage = favoriteRepository.findByUser(currentUser, pageable);

        return PageResponse.from(favoritePage.map(favoriteMapper::toResponse));
    }
}
