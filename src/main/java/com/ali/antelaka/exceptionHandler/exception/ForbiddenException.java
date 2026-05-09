package com.ali.antelaka.exceptionHandler.exception;


import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {
    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    public ForbiddenException(String message, Object additionalData) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN", additionalData);
    }
}