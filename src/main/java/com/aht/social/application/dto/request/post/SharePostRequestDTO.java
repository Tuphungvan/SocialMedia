package com.aht.social.application.dto.request.post;

import com.aht.social.domain.enums.PostPrivacy;
import lombok.Data;

@Data
public class SharePostRequestDTO {
    private String content; // Caption của người share
    private PostPrivacy privacy; // Quyền riêng tư của bài share (thường mặc định là PUBLIC)
}
