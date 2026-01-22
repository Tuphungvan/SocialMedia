package com.aht.social.application.dto.request.post;

import com.aht.social.domain.enums.PostPrivacy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequestDTO {

    @Size(max = 5000, message = "Nội dung không được vượt quá 5000 ký tự")
    private String content;

    @NotNull(message = "Quyền riêng tư không được để trống")
    private PostPrivacy privacy;

    private String location;
    private String feeling;

    @Valid
    private List<PostMediaRequestDTO> media;
}
