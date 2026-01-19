package com.example.blog.dto.request;

import lombok.Getter;

@Getter
public class TokenReissueRequest {
    private String refreshToken;
}
