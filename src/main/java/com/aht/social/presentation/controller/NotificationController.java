package com.aht.social.presentation.controller;

import com.aht.social.application.dto.event.NotificationResponse;
import com.aht.social.application.dto.response.common.ApiResponse;
import com.aht.social.application.dto.response.common.PaginationResponse;
import com.aht.social.application.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<NotificationResponse>>> getNotifications(
            @PageableDefault(size = 20) Pageable pageable) {

        var result = notificationService.getMyNotifications(pageable);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách thông báo thành công"));
    }
}
