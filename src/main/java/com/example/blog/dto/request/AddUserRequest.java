package com.example.blog.dto.request;

import com.example.blog.domain.UserRole;
import lombok.Getter;

@Getter
public class AddUserRequest {
    private String email;
    private String password;
    private UserRole role;
}
