package com.investrac.notification.controller;

import com.investrac.common.response.ApiResponse;
import com.investrac.notification.dto.*;
import com.investrac.notification.entity.Notification;
import com.investrac.notification.entity.NotificationPreference;
import com.investrac.notification.mapper.NotificationMapper;
import com.investrac.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "In-app notifications and preference management")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper  mapper;

    // ── GET NOTIFICATIONS (paged) ──
    @GetMapping
    @Operation(summary = "Get paginated notifications for current user")
    public ResponseEntity<ApiResponse<NotificationPageResponse>> getNotifications(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Notification> pageResult = notificationService.getNotifications(
            userId, page, Math.min(size, 50));

        long unread = notificationService.getUnreadCount(userId);

        NotificationPageResponse response = NotificationPageResponse.builder()
            .content(pageResult.getContent().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList()))
            .pageNumber(pageResult.getNumber())
            .pageSize(pageResult.getSize())
            .totalElements(pageResult.getTotalElements())
            .totalPages(pageResult.getTotalPages())
            .last(pageResult.isLast())
            .unreadCount(unread)
            .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ── UNREAD COUNT ──
    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count for badge display")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
            notificationService.getUnreadCount(userId)));
    }

    // ── MARK ONE READ ──
    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a specific notification as read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        notificationService.markRead(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
    }

    // ── MARK ALL READ ──
    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @RequestHeader("X-User-Id") Long userId) {
        notificationService.markAllRead(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }

    // ── GET PREFERENCES ──
    @GetMapping("/preferences")
    @Operation(summary = "Get notification preferences for current user")
    public ResponseEntity<ApiResponse<PreferenceResponse>> getPreferences(
            @RequestHeader("X-User-Id") Long userId) {
        NotificationPreference prefs = notificationService.getPreferences(userId);
        return ResponseEntity.ok(ApiResponse.success(mapper.toPreferenceResponse(prefs)));
    }

    // ── UPDATE PREFERENCES ──
    @PutMapping("/preferences")
    @Operation(summary = "Update notification preferences (FCM token, email, per-type toggles)")
    public ResponseEntity<ApiResponse<PreferenceResponse>> updatePreferences(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdatePreferenceRequest request) {

        NotificationPreference updated = notificationService.updatePreferences(
            userId,
            request.getPushEnabled(),
            request.getEmailEnabled(),
            request.getFcmToken(),
            request.getEmail(),
            request.getTransactionNotif(),
            request.getEmiNotif(),
            request.getWalletAlertNotif(),
            request.getPortfolioNotif(),
            request.getAiInsightNotif()
        );

        return ResponseEntity.ok(ApiResponse.success(
            mapper.toPreferenceResponse(updated),
            "Preferences updated"));
    }
}
