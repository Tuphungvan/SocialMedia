package com.aht.social.application.mapper;

import com.aht.social.application.dto.response.comment.CommentResponseDTO;
import com.aht.social.domain.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "postId", source = "post.id")
    @Mapping(target = "user", source = "user") // Ánh xạ từ User Entity sang UserResponseDTO
    @Mapping(target = "parentCommentId", source = "parentComment.id")
    @Mapping(target = "isLiked", ignore = true) // Sẽ set thủ công trong Service
    CommentResponseDTO toDTO(Comment comment);

    List<CommentResponseDTO> toDTOList(List<Comment> comments);
}
