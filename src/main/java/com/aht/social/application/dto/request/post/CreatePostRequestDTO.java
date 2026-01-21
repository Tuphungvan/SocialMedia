package com.aht.social.application.dto.request.post;

import com.aht.social.domain.enums.PostPrivacy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

public class CreatePostRequestDTO {
    @NotBlank(message = "Nội dung bài viết không được để trống")
    @Size(max = 5000, message = "Nội dung không được vượt quá 5000 ký tự")
    private String content;

    @NotNull(message = "Quyền riêng tư phải được thiết lập")
    private PostPrivacy privacy; // PUBLIC, FRIENDS, PRIVATE

    private String location;
    private String feeling;

    // Danh sách các URL hình ảnh hoặc video đính kèm
    private List<String> mediaUrls;

    // Nếu bạn muốn lưu thứ tự ảnh ngay từ lúc tạo
    private List<PostMediaRequest> mediaItems;

    @Data
    public static class PostMediaRequest {
        private String mediaUrl;
        private String mediaType; // IMAGE hoặc VIDEO
        private int orderIndex;
    }
}
