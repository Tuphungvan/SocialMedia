package com.aht.social.application.mapper;

import com.aht.social.application.dto.response.auth.UserResponse;
import com.aht.social.application.dto.response.post.UserPublicResponse;
import com.aht.social.domain.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
    UserPublicResponse toPublicResponse(User user);
}
