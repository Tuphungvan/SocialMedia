package com.aht.social.application.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CommentRequestDTO {
    @NotBlank(message = "Nội dung bình luận không được để trống")
    private String content;
    private UUID parentCommentId; // Để trống nếu là comment cấp 1
}
