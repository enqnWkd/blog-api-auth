package com.example.blog.dto.request;

import lombok.Getter;
import org.springframework.stereotype.Service;

@Getter
@Service
public class CommentRequest {
    private Long articleId;
    private String email;
    private String content;
}
