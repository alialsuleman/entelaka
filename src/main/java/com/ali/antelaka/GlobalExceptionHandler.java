package com.ali.antelaka;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {



    @ExceptionHandler(  RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthException2(RuntimeException ex) {
        System.out.println(ex.getMessage()) ;
        ApiResponse<Void> response = new ApiResponse<>(
                false,
                ex.getMessage(),
                null,
                List.of(ex.getLocalizedMessage()),
                LocalDateTime.now(),
                404
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }




    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthException(AuthenticationException ex) {
        ApiResponse<Void> response = new ApiResponse<>(
                false,
                "Authentication Failed",
                null,
                List.of(ex.getMessage()),
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

     @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        ApiResponse<Void> response = new ApiResponse<>(
                false,
                "Access Denied",
                null,
                List.of(ex.getMessage()),
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

     @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllExceptions(Exception ex) {
        System.out.println(ex.toString());
        ApiResponse<Void> response = new ApiResponse<>(
                false,
                "Internal Server Error",
                null,
                List.of(ex.getMessage()),
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.ok(response);
    }


}