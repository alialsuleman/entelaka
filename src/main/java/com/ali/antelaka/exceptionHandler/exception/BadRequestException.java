package com.ali.antelaka.exceptionHandler.exception;


import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }

    public BadRequestException(String message, Object additionalData) {
        super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST", additionalData);
    }
}