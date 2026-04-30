package com.example.qlbds.common.exception;

public class InvalidResourceException extends RuntimeException{
    // Constructor nhận tên resource và lý do để tạo thông báo lỗi chi tiết
    public InvalidResourceException(String ResourceName, String reason) {
        super(String.format("Không hợp lệ với %s: %s", ResourceName, reason));
    }
}
