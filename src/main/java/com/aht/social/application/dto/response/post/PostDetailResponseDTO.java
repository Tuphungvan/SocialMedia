package com.aht.social.application.dto.response.post;

import com.aht.social.application.dto.response.comment.CommentResponseDTO;
import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PostDetailResponseDTO extends PostResponseDTO {
    private List<CommentResponseDTO> comments;
}
