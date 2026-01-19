package com.example.blog.exception;

import com.example.blog.dto.response.ErrorResponse;
import com.example.blog.jwt.AuthErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException e) {

        AuthErrorCode code = e.getErrorCode();

        return ResponseEntity
                .status(code.getStatus())
                .body(new ErrorResponse(
                        code.getError(),
                        code.getMessage()
                ));
    }
}
