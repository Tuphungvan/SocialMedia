package com.aht.social.application.mapper;

import com.aht.social.application.dto.request.post.CreatePostRequestDTO;
import com.aht.social.application.dto.request.post.UpdatePostRequestDTO;
import com.aht.social.application.dto.response.post.PostDetailResponseDTO;
import com.aht.social.application.dto.response.post.PostResponseDTO;
import com.aht.social.domain.entity.Post;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = { UserMapper.class, PostMediaMapper.class })
public interface PostMapper {

    PostResponseDTO toDTO(Post post);

    // Dùng cho API Create
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Post toCreateEntity(CreatePostRequestDTO createDTO);

    // Dùng cho API Update
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Post toUpdateEntity(UpdatePostRequestDTO updateDTO);

    // Method mới dùng cho trang Chi tiết
    @Mapping(target = "comments", ignore = true) // Chúng ta sẽ set comments thủ công trong Service sau khi query
    @Mapping(target = "liked", ignore = true)    // Set thủ công theo User đang đăng nhập
    PostDetailResponseDTO toDetailDTO(Post post);
}
