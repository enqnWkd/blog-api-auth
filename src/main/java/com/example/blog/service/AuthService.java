package com.example.blog.service;

import com.example.blog.domain.RefreshToken;
import com.example.blog.domain.User;
import com.example.blog.dto.response.TokenResponse;
import com.example.blog.jwt.JwtTokenProvider;
import com.example.blog.repository.RefreshTokenRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenResponse login(Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        User user = userDetails.getUser();

        String accessToken = jwtTokenProvider.createAccessToken(user);

        String refreshToken = jwtTokenProvider.createRefreshToken(user);
        LocalDateTime expiredAt = jwtTokenProvider.getRefreshTokenExpiredAt();

        //기존 RT 조회 - 1개 유지 (없으면 새로 생성)
        RefreshToken rt = refreshTokenRepository.findByUser(user)
                .orElse(new RefreshToken(user));

        rt.updateToken(refreshToken, expiredAt);

        refreshTokenRepository.save(rt);

        return new TokenResponse(accessToken, refreshToken);
    }

    public void logout(User user, HttpServletResponse response) {

        refreshTokenRepository.deleteByUser(user);

        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // https면 true
        cookie.setPath("/api/jwt/reissue"); // 발급할 때랑 동일
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }
}
