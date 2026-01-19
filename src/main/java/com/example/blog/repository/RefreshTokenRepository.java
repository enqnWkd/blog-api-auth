package com.example.blog.repository;

import com.example.blog.domain.RefreshToken;
import com.example.blog.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    //재발급 - RefreshToken 엔티티 찾기
    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByTokenAndExpiredAtAfter(
            String token,
            LocalDateTime now
    );

    //로그인,로그아웃 - 특정 유저가 가지고 있는 refresh token 조회
    Optional<RefreshToken> findByUser(User user);

    //로그아웃
    void deleteByUser(User user);
}
