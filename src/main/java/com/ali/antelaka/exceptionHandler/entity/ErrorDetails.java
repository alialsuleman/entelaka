package com.ali.antelaka.exceptionHandler.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetails {

    private String traceId ;
    private String exception;
    private String message;
    private String path;
    private String method;
    private String className;
    private String methodName;
    private Integer lineNumber;
    private LocalDateTime timestamp;

}