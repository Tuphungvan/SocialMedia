package com.aht.social.application.dto.request.post;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CommentRequest {
    @NotBlank(message = "Nội dung bình luận không được để trống")
    private String content;
    private UUID parentCommentId; // Để trống nếu là comment cấp 1
}
