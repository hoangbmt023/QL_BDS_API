package com.example.qlbds.common.exception;

import java.util.Map;

public record ErrorResponse(
        boolean success,
        String message,
        Map<String, String> errors) {
    public static ErrorResponse message(String msg) {
        return new ErrorResponse(false, msg, null);
    }

    public static ErrorResponse validation(Map<String, String> errors) {
        return new ErrorResponse(false, "Không hợp lệ", errors);
    }
}
