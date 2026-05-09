package com.ali.antelaka.exceptionHandler.exception;


import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
    }

    public UnauthorizedException(String message, Object additionalData) {
        super(message, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", additionalData);
    }
}