package com.example.blog.controller;

import com.example.blog.domain.User;
import com.example.blog.dto.request.AddUserRequest;
import com.example.blog.dto.request.LoginRequest;
import com.example.blog.dto.request.TokenReissueRequest;
import com.example.blog.dto.response.TokenResponse;
import com.example.blog.exception.AuthException;
import com.example.blog.jwt.AuthErrorCode;
import com.example.blog.jwt.JwtTokenProvider;
import com.example.blog.repository.RefreshTokenRepository;
import com.example.blog.service.AuthService;
import com.example.blog.service.CustomUserDetails;
import com.example.blog.service.RefreshTokenService;
import com.example.blog.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jwt")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/signup")
    public String signup(@RequestBody AddUserRequest request) {
        userService.save(request);
        return "redirect:/api/jwt/login";
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(), request.getPassword())
                );

        TokenResponse tokenResponse = authService.login(authentication);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", tokenResponse.getRefreshToken())
                .httpOnly(true)
                .secure(false) // 로컬 테스트라 false
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();
        //
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new TokenResponse(tokenResponse.getAccessToken(), null));
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        if (refreshToken == null) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        TokenResponse response = refreshTokenService.reissue(refreshToken);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new TokenResponse(response.getAccessToken(), null));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response
    ) {
        authService.logout(userDetails.getUser(), response);
        return ResponseEntity.noContent().build();
    }
}
