package com.aht.social.application.dto.response.comment;

import com.aht.social.application.dto.response.post.UserPublicResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDTO {
    private UUID id;
    private UUID postId;
    private UserPublicResponse user; // Chứa id, username, avatarUrl
    private String content;
    private int likesCount;
    private int repliesCount; // Số lượng phản hồi của comment này
    private boolean isLiked;  // Trạng thái của người đang xem
    private UUID parentCommentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}