package com.ali.antelaka.exceptionHandler.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {
    public NotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "NOT_FOUND");
    }

    public NotFoundException(String message, Object additionalData) {
        super(message, HttpStatus.NOT_FOUND, "NOT_FOUND", additionalData);
    }
}