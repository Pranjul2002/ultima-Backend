package com.edutech.edutechbackend.exception;

/**
 * Thrown for invalid client input that doesn't fit a validation annotation.
 * Maps to HTTP 400.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}