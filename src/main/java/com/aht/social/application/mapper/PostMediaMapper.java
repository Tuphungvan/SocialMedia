package com.aht.social.application.mapper;

import com.aht.social.application.dto.request.post.PostMediaRequestDTO;
import com.aht.social.application.dto.response.post.PostMediaResponseDTO;
import com.aht.social.domain.entity.PostMedia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostMediaMapper {

    @Mapping(target = "post", ignore = true) // post sẽ được set ở service khi gắn vào Post
    PostMedia toEntity(PostMediaRequestDTO dto);

    PostMediaResponseDTO toResponse(PostMedia entity);
}
