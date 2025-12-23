package com.example.blog.exception;

import com.example.blog.jwt.AuthErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiErrorResponse {
    private int status;
    private String error;
    private String message;

    public static ApiErrorResponse from(AuthErrorCode code) {
        return new ApiErrorResponse(
                code.getStatus(),
                code.getError(),
                code.getMessage()
        );
    }
}
