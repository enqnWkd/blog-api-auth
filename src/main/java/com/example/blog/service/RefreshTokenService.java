package com.example.blog.service;

import com.example.blog.domain.RefreshToken;
import com.example.blog.domain.User;
import com.example.blog.dto.response.TokenResponse;
import com.example.blog.exception.AuthException;
import com.example.blog.jwt.AuthErrorCode;
import com.example.blog.jwt.JwtTokenProvider;
import com.example.blog.repository.RefreshTokenRepository;
import com.example.blog.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public TokenResponse reissue(String requestRefreshToken) {

        Claims claims = jwtTokenProvider.parseClaimsAllowExpired(requestRefreshToken);

        if (!"REFRESH".equals(claims.get("type", String.class))) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        String email = claims.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_TOKEN));

        //DB상의 rt와 요청 rt가 같은지 비교
        RefreshToken storedRefreshToken = refreshTokenRepository.findByUser(user)
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_TOKEN));

        if (!storedRefreshToken.getToken().equals(requestRefreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        //RT 만료 검사
        if (storedRefreshToken.isExpired()) {
            refreshTokenRepository.delete(storedRefreshToken);
            throw new AuthException(AuthErrorCode.EXPIRED_TOKEN);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(user);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user);
        LocalDateTime newExpiredAt = jwtTokenProvider.getRefreshTokenExpiredAt();

        storedRefreshToken.updateToken(newRefreshToken, newExpiredAt);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

}
