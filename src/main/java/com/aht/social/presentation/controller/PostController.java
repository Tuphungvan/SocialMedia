package com.aht.social.presentation.controller;

import com.aht.social.application.dto.request.post.CreatePostRequestDTO;
import com.aht.social.application.dto.response.common.ApiResponse;
import com.aht.social.application.dto.response.common.PaginationResponse;
import com.aht.social.application.dto.response.post.PostResponseDTO;
import com.aht.social.application.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponseDTO>> createPost(@Valid @RequestBody CreatePostRequestDTO request){
        PostResponseDTO response = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tạo post thành công"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<PostResponseDTO>>> getNewsfeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        PaginationResponse<PostResponseDTO> data = postService.getNewsfeed(page, size);
        return ResponseEntity.ok(ApiResponse.success(data,"Lấy danh sách post thành công"));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponseDTO>> getPostDetail(
            @PathVariable UUID postId
    ) {
        PostResponseDTO response = postService.getPostDetail(postId);
        return ResponseEntity.ok(ApiResponse.success(response, "Chi tiết bài viết"));
    }
}
