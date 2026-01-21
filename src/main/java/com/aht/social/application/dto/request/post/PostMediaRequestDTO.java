package com.aht.social.application.dto.request.post;

import com.aht.social.domain.enums.MediaType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostMediaRequestDTO {

    @NotNull(message = "Loại media không được để trống")
    private MediaType mediaType; // IMAGE, VIDEO

    @NotBlank(message = "Đường dẫn media không được để trống")
    private String mediaUrl;

    private String thumbnailUrl;

    @Min(value = 0, message = "Thứ tự media không hợp lệ")
    private Integer orderIndex;
}
