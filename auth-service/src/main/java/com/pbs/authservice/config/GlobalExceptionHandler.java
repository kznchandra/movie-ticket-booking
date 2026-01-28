package com.pbs.authservice.config;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatusCode.valueOf(500), e.getMessage()
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleRuntimeException(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatusCode.valueOf(500), e.getMessage()
        );
    }
}
