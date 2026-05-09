package com.ali.antelaka.exceptionHandler.exception;


import org.springframework.http.HttpStatus;

public class ConflictException extends BaseException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "CONFLICT");
    }

    public ConflictException(String message, Object additionalData) {
        super(message, HttpStatus.CONFLICT, "CONFLICT", additionalData);
    }
}