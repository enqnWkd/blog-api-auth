package com.example.blog.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JwtErrorCode {

    EXPIRED_TOKEN(401, "TOKEN_EXPIRED", "토큰이 만료되었습니다"),
    INVALID_TOKEN(401, "INVALID_TOKEN", "유효하지 않은 토큰입니다"),
    ACCESS_DENIED(403, "ACCESS_DENIED", "접근 권한이 없습니다");

    private final int status;
    private final String error;
    private final String message;
}
