package com.aht.social.application.dto.request.post;

import com.aht.social.domain.enums.PostPrivacy;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdatePostRequestDTO {

    @Size(max = 5000, message = "Nội dung không được vượt quá 5000 ký tự")
    private String content;

    private PostPrivacy privacy;

    private String location;
    private String feeling;

    // Cờ đánh dấu để cập nhật trạng thái is_edited = true trong DB
    private boolean isEdited = true;

    // Danh sách media mới nếu người dùng thay đổi ảnh/video
    private List<String> newMediaUrls;
}
