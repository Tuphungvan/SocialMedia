package com.aht.social.application.mapper;


import com.aht.social.domain.enitity.Post;
import com.aht.social.application.dto.request.post.CreatePostRequestDTO;
import com.aht.social.application.dto.request.post.UpdatePostRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface PostMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "userAvatar", source = "user.avatarUrl")
    CreatePostRequestDTO toDTO(Post post);

    @Mapping(target = "user", ignore = true) // User sẽ được set thủ công từ Security Context
    Post toEntity(UpdatePostRequestDTO postDTO);
}
