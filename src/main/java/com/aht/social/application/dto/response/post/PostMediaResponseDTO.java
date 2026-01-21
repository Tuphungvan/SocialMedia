package com.aht.social.application.dto.response.post;

import com.aht.social.domain.enums.MediaType;
import lombok.Data;

import java.util.UUID;

@Data
public class PostMediaResponseDTO {
    private UUID id;
    private MediaType mediaType;
    private String mediaUrl;
    private String thumbnailUrl;
    private int orderIndex;
}
