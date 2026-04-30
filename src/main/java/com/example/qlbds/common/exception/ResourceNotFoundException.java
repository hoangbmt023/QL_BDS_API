package com.example.qlbds.common.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s not found with ID: '%s'", resourceName, id));
    }
}
