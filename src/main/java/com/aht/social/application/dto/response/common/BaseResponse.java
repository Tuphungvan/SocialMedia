package com.aht.social.application.dto.response.common;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaseResponse<T> {
    int status;
    String message;
    T data;
}
