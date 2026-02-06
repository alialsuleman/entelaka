package com.ali.antelaka.notification.controller;


 import com.ali.antelaka.ApiResponse;
 import com.ali.antelaka.notification.service.NotificationPreferenceService;
 import com.ali.antelaka.user.entity.User;
 import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.web.bind.annotation.*;

 import java.security.Principal;
 import java.time.LocalDateTime;

@RestController
@RequestMapping("/notification-preferences")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class NotificationPreferenceController {

    private final NotificationPreferenceService preferenceService;

    @PostMapping("/fcm-token")
    public ResponseEntity<ApiResponse<Void>> saveFcmToken(
            Principal connectedUser,
            @RequestParam String fcmToken) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        preferenceService.saveFcmToken(user.getId(), fcmToken);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("FCM token saved successfully")
                .timestamp(LocalDateTime.now())
                .status(200)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/push-enabled")
    public ResponseEntity<ApiResponse<Void>> updatePushEnabled(
            Principal connectedUser,
            @RequestParam boolean enabled) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        preferenceService.updatePushEnabled(user.getId(), enabled);

        String message = enabled ?
                "Push notifications enabled" : "Push notifications disabled";

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .status(200)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/fcm-token")
    public ResponseEntity<ApiResponse<Void>> removeFcmToken(
            Principal connectedUser
    ) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        preferenceService.removeFcmToken(user.getId());

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("FCM token removed successfully")
                .timestamp(LocalDateTime.now())
                .status(200)
                .build();

        return ResponseEntity.ok(response);
    }
}