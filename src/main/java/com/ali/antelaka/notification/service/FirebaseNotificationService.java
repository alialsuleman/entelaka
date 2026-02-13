package com.ali.antelaka.notification.service;

 import com.ali.antelaka.notification.entity.NotificationPreferenceRepository;
 import com.ali.antelaka.notification.entity.NotificationResponse;
 import com.ali.antelaka.notification.entity.NotificationType;
 import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
 import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

 @Service
@RequiredArgsConstructor
public class FirebaseNotificationService {

    private final FirebaseMessaging firebaseMessaging;
    private final NotificationPreferenceRepository preferenceRepository;

    public void sendPushNotification(NotificationResponse notification) {
        try {
            // البحث عن تفضيلات المستخدم
            var preferenceOptional = preferenceRepository.findByUserId(notification.getUserId());
            System.out.println("Push notification sent successfully 1 ") ;
            if (preferenceOptional.isEmpty()) {
                System.out.println("No notification preference found for user: {}"   + notification.getUserId());
                return;
            }

            var preference = preferenceOptional.get();

            // التحقق من تفعيل الإشعارات ووجود FCM token
            if (!preference.isPushEnabled() || preference.getFcmToken() == null || preference.getFcmToken().isEmpty()) {
                System.out.println("Push notifications disabled or no FCM token for user: {}" + notification.getUserId());
                return;
            }

            // إنشاء بيانات الإشعار
            Map<String, String> data = new HashMap<>();
            data.put("notificationId", notification.getId().toString());
            data.put("type", notification.getType().toString());
            if (notification.getType() == NotificationType.NEW_FOLLOWER)
            {
                data.put("entityId", notification.getSenderId() != null ?
                        notification.getSenderId().toString() : "");
            }else  data.put("entityId", notification.getEntityId() != null ?
                    notification.getEntityId().toString() : "");
            data.put("isAggregated", String.valueOf(notification.isAggregated()));

            // إنشاء الإشعار
            com.google.firebase.messaging.Notification firebaseNotification =
                    com.google.firebase.messaging.Notification.builder()
                            .setTitle(getNotificationTitle(notification.getType()))
                            .setBody(notification.getMessage())
                            .build();

            Message message = Message.builder()
                    .setToken(preference.getFcmToken())
                    .setNotification(firebaseNotification)
                    .putAllData(data)
                    .build();
            System.out.println("Push notification sent successfully "  ) ;
            // إرسال الإشعار
            String response = firebaseMessaging.send(message);
            System.out.println("Push notification sent successfully for user {}: {}"+
                    notification.getUserId()+ response);

        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
               // log.warn("FCM token is no longer valid for user: {}. Removing token.",
                     ///   notification.getUserId());
                removeInvalidToken(notification.getUserId());
            } else {
               // log.error("Failed to send push notification for user: {}",
             //           notification.getUserId(), e);
            }
        } catch (Exception e) {
             // log.error("Unexpected error sending push notification for user: {}",
                  //  notification.getUserId(), e);
        }
    }

    private void removeInvalidToken(Integer userId) {
        preferenceRepository.findByUserId(userId).ifPresent(preference -> {
            preference.setFcmToken(null);
            preferenceRepository.save(preference);
            //log.info("Removed invalid FCM token for user: {}", userId);
        });
    }

    private String getNotificationTitle(NotificationType type) {
        switch (type) {
            case PASSWORD_CHANGED:
                return "Password Changed";
            case NEW_FOLLOWER:
                return "New Follower";
            case POST_LIKE:
                return "New Like";
            case POST_COMMENT:
                return "New Comment";
            case COMMENT_REPLY:
                return "New Reply";
            default:
                return "New Notification";
        }
    }
}