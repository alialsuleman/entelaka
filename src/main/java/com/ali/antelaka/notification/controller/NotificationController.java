package com.ali.antelaka.notification.controller;

import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.notification.entity.NotificationRequest;
import com.ali.antelaka.notification.entity.NotificationResponse;
import com.ali.antelaka.notification.service.NotificationService;
import com.ali.antelaka.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            Principal connectedUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        Sort sort = direction.equalsIgnoreCase("ASC") ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        Page<NotificationResponse> notifications =
                notificationService.getUserNotifications(user.getId(), pageable);

        ApiResponse<Page<NotificationResponse>> response = ApiResponse.<Page<NotificationResponse>>builder()
                .success(true)
                .message("Notifications retrieved successfully")
                .data(notifications)
                .timestamp(LocalDateTime.now())
                .status(200)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUnreadNotifications(
            Principal connectedUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<NotificationResponse> notifications =
                notificationService.getUnreadNotifications(user.getId(), pageable);

        ApiResponse<Page<NotificationResponse>> response = ApiResponse.<Page<NotificationResponse>>builder()
                .success(true)
                .message("Unread notifications retrieved successfully")
                .data(notifications)
                .timestamp(LocalDateTime.now())
                .status(200)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            Principal connectedUser
    ) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        long count = notificationService.getUnreadCount(user.getId());

        ApiResponse<Long> response = ApiResponse.<Long>builder()
                .success(true)
                .message("Unread count retrieved successfully")
                .data(count)
                .timestamp(LocalDateTime.now())
                .status(200)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            Principal connectedUser,
            @PathVariable Long id) {


        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        notificationService.markAsRead(id, user.getId());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Notification marked as read")
                .timestamp(LocalDateTime.now())
                .status(200)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            Principal connectedUser
    ) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        notificationService.markAllAsRead( user.getId());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("All notifications marked as read")
                .timestamp(LocalDateTime.now())
                .status(200)
                .build();

        return ResponseEntity.ok(response);
    }

    // Endpoint للإشعارات المباشرة (للاستخدام من خدمات أخرى)
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<NotificationResponse>> sendNotification(
            @RequestBody NotificationRequest request) {

        NotificationResponse notification = notificationService.createNotification(request);

        ApiResponse<NotificationResponse> response = ApiResponse.<NotificationResponse>builder()
                .success(true)
                .message("Notification sent successfully")
                .data(notification)
                .timestamp(LocalDateTime.now())
                .status(200)
                .build();

        return ResponseEntity.ok(response);
    }
}