package com.aht.social.presentation.controller;

import com.aht.social.application.dto.request.post.CommentRequest;
import com.aht.social.application.dto.response.comment.CommentResponseDTO;
import com.aht.social.application.dto.response.common.ApiResponse;
import com.aht.social.application.dto.response.common.PaginationResponse;
import com.aht.social.application.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    
    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponseDTO>> addComment(
            @PathVariable UUID postId,
            @RequestBody @Valid CommentRequest request) {

        CommentResponseDTO response = commentService.addComment(postId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Đã gửi bình luận"));
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<PaginationResponse<CommentResponseDTO>>> getComments(
            @PathVariable UUID postId,
            @PageableDefault(size = 10) Pageable pageable) {

        var comments = commentService.getCommentsByPostId(postId, pageable);
        return ResponseEntity.ok(ApiResponse.success(comments, "Lấy danh sách bình luận thành công"));
    }
}
