package com.orchid_backend.util.error;

public class DuplicatedObjectException extends RuntimeException {
    public DuplicatedObjectException(String message) {
        super(message);
    }
}
