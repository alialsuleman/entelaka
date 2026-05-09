package com.ali.antelaka.exceptionHandler.handler;


import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.exceptionHandler.entity.ErrorDetails;
import com.ali.antelaka.exceptionHandler.exception.BaseException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {





    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<?>> handleBaseException(BaseException ex, HttpServletRequest request) {

        // log section
        ErrorDetails errorDetails = createErrorDetails(ex, request ) ;


        // api response section
        HashMap<String , String> errorData = new HashMap<String , String>() ;
        errorData.put("traceId" , errorDetails.getTraceId() );

        ApiResponse<?> response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .data(errorData)
                .errors(List.of(ex.getMessage()))
                .timestamp(LocalDateTime.now())
                .status(ex.getStatus().value())
                .build();



        return ResponseEntity
                .status(ex.getStatus())
                .body(response);


    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(
            Exception ex,
            HttpServletRequest request
    ) {


        // log section
        ErrorDetails errorDetails = createErrorDetails(ex, request ) ;



        // api response section
        HashMap<String , String> errorData = new HashMap<String , String>() ;
        errorData.put("traceId" , errorDetails.getTraceId() );


        ApiResponse<?> response = ApiResponse.builder()
                .success(false)
                .message("Internal server error")
                .data(errorData)
                .errors(List.of("Unexpected error occurred"))
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();



        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }



    private ErrorDetails createErrorDetails (
            Exception ex,
            HttpServletRequest request )
    {

        String traceId = UUID.randomUUID().toString().substring(0, 8);


        StackTraceElement element = ex.getStackTrace()[0];
        return ErrorDetails.builder()
                .traceId(traceId)
                .exception(ex.getClass().getSimpleName())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .method(request.getMethod())
                .className(element.getClassName())
                .methodName(element.getMethodName())
                .lineNumber(element.getLineNumber())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
