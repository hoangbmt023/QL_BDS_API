package com.example.qlbds.common.exception;

public class ResourceNotFoundException extends RuntimeException {

    /** Thông báo tùy ý */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /** Chuẩn hóa dạng "Resource không tồn tại với ID: 'x'" */
    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s không tồn tại với ID: '%s'", resourceName, id));
    }
}
