package com.example.qlbds.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.example.qlbds.common.response.PageResponse;
import com.example.qlbds.common.response.PaginationMeta;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private PaginationMeta pagination;
    private String errorCode;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<List<T>> paginatedSuccess(PageResponse<T> pageResponse, String message) {
        PaginationMeta meta = PaginationMeta.builder()
                .currentPage(pageResponse.getCurrentPage())
                .pageSize(pageResponse.getPageSize())
                .totalElements(pageResponse.getTotalElements())
                .totalPages(pageResponse.getTotalPages())
                .last(pageResponse.isLast())
                .build();
                
        return ApiResponse.<List<T>>builder()
                .success(true)
                .message(message)
                .data(pageResponse.getData())
                .pagination(meta)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}
