package com.aht.social.application.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
public class CommentUpdateRequest {
    @NotBlank(message = "Nội dung bình luận không được để trống")
    private String content;
}