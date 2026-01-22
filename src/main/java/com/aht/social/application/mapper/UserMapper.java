package com.aht.social.application.mapper;

import com.aht.social.application.dto.response.auth.UserResponseDTO;
import com.aht.social.application.dto.response.post.UserPublicResponse;
import com.aht.social.domain.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDTO toResponse(User user);
    UserPublicResponse toPublicResponse(User user);
}
