package com.edutech.edutechbackend.exception;

/**
 * Thrown when a create/update would violate a uniqueness constraint.
 * Maps to HTTP 409.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}