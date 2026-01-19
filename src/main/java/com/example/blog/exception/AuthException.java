package com.example.blog.exception;

import com.example.blog.jwt.AuthErrorCode;
import lombok.Getter;

@Getter
//인증 인가 예외 처리
public class AuthException extends RuntimeException {

    private final AuthErrorCode errorCode;

    public AuthException(AuthErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
