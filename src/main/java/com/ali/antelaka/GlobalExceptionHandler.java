package com.ali.antelaka;

import com.ali.antelaka.exception.CustomAuthenticationException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(CustomAuthenticationException.class)
    public ResponseEntity<ApiResponse< ?>> handleCustomAuthenticationException(CustomAuthenticationException ex) {
        Map m = new HashMap<String, String>( ) ;
        m.put("accessToken" , null);
        m.put("refreshToken" , null);
        m.put("verified" , false);

        ApiResponse<?> response = new ApiResponse<>(
                false,
                "Authentication Failed",
                m,
                List.of(ex.getMessage()),
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }




    @ExceptionHandler(  RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthException2(RuntimeException ex) {
        System.out.println("hello1");
        System.out.println(ex.getMessage()) ;
        Map m = new HashMap<String, String>( ) ;
        if (ex.getMessage().equals("You must confirm your email first."))
        {
            m.put("accessToken" , null);
            m.put("refreshToken" , null);
            m.put("verified" , false);
        }
        ApiResponse<?> response = new ApiResponse<>(
                false,
                ex.getMessage(),
                m,
                List.of(ex.getLocalizedMessage()),
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(400).body(response);
    }




    @ExceptionHandler({AuthenticationException.class, javax.security.sasl.AuthenticationException.class})
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
        return ResponseEntity.status(response.getStatus()).body(response);
    }


}