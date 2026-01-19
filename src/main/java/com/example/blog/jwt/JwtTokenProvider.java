package com.example.blog.jwt;
import com.example.blog.domain.User;
import com.example.blog.service.CustomUserDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final long ACCESS_TOKEN_EXPIRATION_TIME = 1000L * 60; //
    private final long REFRESH_TOKEN_EXPIRATION_TIME = 1000L * 60 * 60 * 24; // 24시간

    private final SecretKey key;
    private final CustomUserDetailsService customUserDetailService;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey, CustomUserDetailsService customUserDetailService) {
        this.customUserDetailService = customUserDetailService;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes); //SecretKey 초기화
    }

    public String createAccessToken(User user) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + ACCESS_TOKEN_EXPIRATION_TIME);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("roles", List.of(user.getRole().name()))
                .claim("type", "ACCESS")
                .issuedAt(new Date())
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + REFRESH_TOKEN_EXPIRATION_TIME);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("type", "REFRESH")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public LocalDateTime getRefreshTokenExpiredAt() {
        return LocalDateTime.now()
                .plusSeconds(REFRESH_TOKEN_EXPIRATION_TIME / 1000);
    }

    //토큰 유효성 검증
    public void validateToken(String token, String expectedType) {

        Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        String type = claims.get("type", String.class);

        if (!expectedType.equals(type)) {
            throw new JwtException("Invalid token type");
        }
    }

    //요청 헤더에서 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7); // "Bearer " 이후 문자열만 추출
        }
        return null;
    }

    //인증 정보 생성
    public Authentication parseAuthentication(String token) {
        Claims claims = parseClaims(token);

        String email = claims.getSubject();
        List<String> roles = claims.get("roles", List.class);

        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
                .toList();

        UserDetails userDetails =
                customUserDetailService.loadUserByUsername(email);

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims parseClaimsAllowExpired(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

}
