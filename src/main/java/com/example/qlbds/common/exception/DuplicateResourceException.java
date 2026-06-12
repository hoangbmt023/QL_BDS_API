package com.example.qlbds.common.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String ResourceName, String reason) {
        super(String.format("Trùng với %s: %s", ResourceName, reason));
    }
}
