package com.example.qlbds.favorite_service.service;

import com.example.qlbds.common.response.PageResponse;
import com.example.qlbds.favorite_service.dto.DeleteMultipleFavoritesRequest;
import com.example.qlbds.favorite_service.dto.FavoriteActionResponse;
import com.example.qlbds.favorite_service.dto.FavoriteRequest;
import com.example.qlbds.favorite_service.dto.FavoriteResponse;

public interface FavoriteService {
    FavoriteActionResponse addFavorite(FavoriteRequest request);

    FavoriteActionResponse removeFavorite(Long propertyId);

    void removeMultipleFavorites(DeleteMultipleFavoritesRequest request);

    void removeAllFavorites();

    FavoriteActionResponse checkFavorite(Long propertyId);

    PageResponse<FavoriteResponse> getMyFavorites(int page, int size);
}
