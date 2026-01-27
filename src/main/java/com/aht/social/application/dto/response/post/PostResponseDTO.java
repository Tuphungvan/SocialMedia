package com.aht.social.application.dto.response.post;

import com.aht.social.application.dto.response.auth.UserResponse;
import com.aht.social.domain.enums.PostPrivacy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDTO {

    private UUID id;
    private String content;
    private PostPrivacy privacy; // PUBLIC, FRIENDS, PRIVATE
    private String location;
    private String feeling;
    private boolean isEdited;
    private int likesCount;
    private int commentsCount;
    private int sharesCount;
    private List<PostMediaResponseDTO> media;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserPublicResponse user;

    private boolean isLiked = false;
    private boolean isSaved = false;
}
