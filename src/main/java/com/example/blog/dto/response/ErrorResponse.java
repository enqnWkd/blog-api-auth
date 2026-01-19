package com.example.blog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private String error;
    private String message;

}
