package com.aht.social.application.dto.response.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPublicResponse {
    private UUID id;
    private String username;
    private String avatarUrl;
}
