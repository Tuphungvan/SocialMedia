package com.aht.social.presentation.controller;

import com.aht.social.application.dto.request.post.CreatePostRequestDTO;
import com.aht.social.application.dto.request.post.SharePostRequestDTO;
import com.aht.social.application.dto.request.post.UpdatePostRequestDTO;
import com.aht.social.application.dto.response.common.ApiResponse;
import com.aht.social.application.dto.response.common.PaginationResponse;
import com.aht.social.application.dto.response.post.PostDetailResponseDTO;
import com.aht.social.application.dto.response.post.PostResponseDTO;
import com.aht.social.application.dto.response.post.UserPublicResponse;
import com.aht.social.application.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    @GetMapping("/newsfeed")
    public ResponseEntity<ApiResponse<PaginationResponse<PostResponseDTO>>> getNewsfeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        PaginationResponse<PostResponseDTO> data = postService.getNewsfeed(page, size);
        return ResponseEntity.ok(ApiResponse.success(data,"Lấy danh sách post thành công"));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponseDTO>> getPostDetail(
            @PathVariable UUID postId
    ) {
        PostDetailResponseDTO response = postService.getPostDetail(postId);
        return ResponseEntity.ok(ApiResponse.success(response, "Chi tiết bài viết"));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable UUID postId,
            @Valid @RequestBody UpdatePostRequestDTO requestDTO) {
        return ResponseEntity.ok(postService.updatePost(postId, requestDTO));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/toggle-like")
    public ResponseEntity<ApiResponse<Boolean>> toggleLike(@PathVariable UUID postId) {
        boolean isLiked = postService.toggleLike(postId);
        String message = isLiked ? "Đã thích bài viết" : "Đã bỏ thích bài viết";
        return ResponseEntity.ok(ApiResponse.success(isLiked, message));
    }

    @GetMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse<PaginationResponse<UserPublicResponse>>> getPostLikes(
            @PathVariable UUID postId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        PaginationResponse<UserPublicResponse> likes = postService.getLikesByPostId(postId, pageable);
        return ResponseEntity.ok(ApiResponse.success(likes, "Lấy danh sách người thích thành công"));
    }

    @PostMapping("/{postId}/share")
    public ResponseEntity<ApiResponse<PostResponseDTO>> sharePost(
            @PathVariable UUID postId,
            @RequestBody @Valid SharePostRequestDTO requestDTO) {

        PostResponseDTO result = postService.sharePost(postId, requestDTO);
        return ResponseEntity.ok(ApiResponse.success(result, "Chia sẻ bài viết thành công"));
    }
}
