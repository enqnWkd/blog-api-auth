package com.example.blog.jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final String SECRET_KEY = "thisIsMySuperSuperLongJwtSecretKeyForAlgorithmHS256!!!"; // 실제로는 환경변수로 관리해야 함
    private final long EXPIRATION_TIME = 1000L * 60 * 60; // 1시간

    private final UserDetailsService userDetailsService;
    private final SecretKey key;

    public JwtTokenProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes()); //SecretKey 초기화
    }

    // 🔹 1️⃣ 토큰 생성
    public String createToken(Authentication authentication) {
        String username = authentication.getName();

        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    //요청 헤더에서 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        System.out.println("\nJwtTokenProvider-resolverToken()\n");
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후 문자열만 추출
        }
        return null;
    }

    //토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    //인증 정보 생성
    public Authentication getAuthentication(String token) {
        String username = getUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
    }

     public String getUsername(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
