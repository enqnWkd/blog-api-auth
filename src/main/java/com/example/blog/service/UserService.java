package com.example.blog.service;

import com.example.blog.domain.User;
import com.example.blog.domain.UserRole;
import com.example.blog.dto.request.AddUserRequest;
import com.example.blog.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private UserRepository userRepository;
    private BCryptPasswordEncoder encoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    public User save(AddUserRequest request) {
        System.out.println("email: " + request.getEmail());
        System.out.println("pw: " + request.getPassword());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + request.getEmail());
        }
        return userRepository.save( //securityConfig에서 비밀번호 암호화 후 db에 저장
                User.builder()
                        .email(request.getEmail())
                        .password(encoder.encode(request.getPassword()))
                        .role(UserRole.USER)
                        .build()
        );
    }
}
