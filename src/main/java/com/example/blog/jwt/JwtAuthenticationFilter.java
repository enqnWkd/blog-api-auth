package com.example.blog.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

//@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/jwt/login")
                || path.equals("/api/jwt/signup")
                || path.equals("/api/jwt/reissue");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {

        String token = jwtTokenProvider.resolveToken(request);

        if (token != null) {
            try {
                jwtTokenProvider.validateToken(token, "ACCESS");
                Authentication authentication =
                        jwtTokenProvider.parseAuthentication(token);
                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);

            } catch (ExpiredJwtException e) {
                request.setAttribute("exception", "EXPIRED_AT");

            } catch (JwtException e) {
                request.setAttribute("exception", "INVALID_AT");
            }

        }
        filterChain.doFilter(request, response);
    }
}
