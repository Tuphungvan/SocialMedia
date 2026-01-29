package com.aht.social.presentation.controller;

import com.aht.social.application.dto.request.comment.CommentRequestDTO;
import com.aht.social.application.dto.request.comment.CommentUpdateRequest;
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
@RequestMapping("/api/v1/posts/{postId}/comments") // Cấu trúc gốc
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;


    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponseDTO>> addComment(
            @PathVariable UUID postId,
            @RequestBody @Valid CommentRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(commentService.addComment(postId, request), "Đã gửi bình luận"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<CommentResponseDTO>>> getComments(
            @PathVariable UUID postId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getCommentsByPostId(postId, pageable), "Lấy danh sách thành công"));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponseDTO>> updateComment(
            @PathVariable UUID postId,
            @PathVariable UUID commentId,
            @RequestBody @Valid CommentUpdateRequest request){
        return ResponseEntity.ok(ApiResponse.success(commentService.updateComment(postId, commentId, request), "Sửa thành công"));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable UUID postId,
            @PathVariable UUID commentId) {
        commentService.deleteComment(postId, commentId);
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa thành công"));
    }

    @PostMapping("/{commentId}/like")
    public ResponseEntity<ApiResponse<Void>> toggleLike(
            @PathVariable UUID postId,
            @PathVariable UUID commentId) {
        commentService.toggleLikeComment(postId, commentId);
        return ResponseEntity.ok(ApiResponse.success(null, "Thao tác thành công"));
    }

    @PostMapping("/{commentId}/reply")
    public ResponseEntity<ApiResponse<CommentResponseDTO>> replyComment(
            @PathVariable UUID postId,
            @PathVariable UUID commentId,
            @RequestBody @Valid CommentRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success(commentService.replyComment(postId, commentId, request), "Đã phản hồi"));
    }

    @GetMapping("/{commentId}/replies")
    public ResponseEntity<ApiResponse<PaginationResponse<CommentResponseDTO>>> getReplies(
            @PathVariable UUID postId,
            @PathVariable UUID commentId,
            @PageableDefault(size = 5, sort = "createdAt") Pageable pageable) {

        var replies = commentService.getRepliesByCommentId(postId, commentId, pageable);
        return ResponseEntity.ok(ApiResponse.success(replies, "Lấy danh sách phản hồi thành công"));
    }
}
