package com.example.blog.controller;

import com.example.blog.domain.Article;
import com.example.blog.dto.request.AddArticleRequest;
import com.example.blog.dto.request.UpdateArticleRequest;
import com.example.blog.dto.response.ArticleResponse;
import com.example.blog.repository.UserRepository;
import com.example.blog.service.BlogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BlogController {
    private BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    //글 저장
    @PostMapping("/api/articles")
    public ResponseEntity<Article> addArticle(
            @RequestBody AddArticleRequest request,
            @AuthenticationPrincipal String email
    ) {
        Article savedArticle = blogService.save(request, email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedArticle);
    }

    //전체 글 조회
    @GetMapping("/api/articles")
    public ResponseEntity<List<ArticleResponse>> findAllArticles(@AuthenticationPrincipal String email) {
        List<ArticleResponse> list = blogService.findAll()
                .stream().map(ArticleResponse::new)
                .toList();
        return ResponseEntity.status(HttpStatus.OK)
                .body(list);
    }

    //특정 글 조회
    @GetMapping("/api/articles/{id}")
    public ResponseEntity<ArticleResponse> findArticle(@PathVariable("id") Long id) {
        ArticleResponse articleResponse = blogService.findArticle(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(articleResponse);
        //return ResponseEntity.ok(articleResponse);
    }

    //전체 글 삭제
    @DeleteMapping("/api/articles")
    public ResponseEntity<Void> deleteAllArticles() {
        blogService.deleteAll();
        return ResponseEntity.ok().build();
    }

    //특정 글 삭제
    @DeleteMapping("/api/articles/{id}")
    public ResponseEntity<Void> deleteArticles(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal String email
    ) {
        blogService.delete(id, email);
        return ResponseEntity.ok().build();
    }

    //글 수정
    @Transactional
    @PutMapping("/api/articles/{id}")
    public ResponseEntity<ArticleResponse> updateArticle(
            @PathVariable("id") Long id,
            @RequestBody UpdateArticleRequest request,
            @AuthenticationPrincipal String email
            ) {
        Article updatedArticle = blogService.update(id, request, email);
        return ResponseEntity.ok(new ArticleResponse(updatedArticle));
    }
}
