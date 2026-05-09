package com.ali.antelaka.exceptionHandler.exception;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@Builder
public abstract class BaseException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;
    private final Object additionalData;

    public BaseException(String message, HttpStatus status, String errorCode) {
        this(message, status, errorCode, null);
    }

    public BaseException(String message, HttpStatus status, String errorCode, Object additionalData) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.additionalData = additionalData;
    }

    public BaseException(String message, HttpStatus status, String errorCode, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
        this.additionalData = null;
    }
}