package com.ali.antelaka.notification.service;


import com.ali.antelaka.notification.entity.*;
import com.ali.antelaka.user.UserRepository;
import com.ali.antelaka.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;




@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FirebaseNotificationService firebaseNotificationService;

    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
//        if (shouldAggregate(request)) {
//            System.out.println("nnnnn");
//
//            var x = handleAggregatedNotification(request);
//            System.out.println("nnnnn");
//            firebaseNotificationService.sendPushNotification(x);
//            return x ;
//
//        }

        Notification notification = buildNotification(request, false);
        Notification savedNotification = notificationRepository.save(notification);
        NotificationResponse response = convertToResponse(savedNotification);
        System.out.println("nnnnn");
        firebaseNotificationService.sendPushNotification(response);

        return response;
    }

    private boolean shouldAggregate(NotificationRequest request) {
        return request.getType() == NotificationType.POST_LIKE ||
                request.getType() == NotificationType.POST_COMMENT ||
                request.getType() == NotificationType.COMMENT_REPLY;
    }

    private NotificationResponse handleAggregatedNotification(NotificationRequest request) {
        LocalDateTime timeLimit = LocalDateTime.now().minusHours(24);

        List<Notification> similarNotifications = notificationRepository
                .findAggregatableNotifications(
                        request.getUserId(),
                        request.getType(),
                        request.getEntityId()
                );

        Optional<Notification> existingAggregated = notificationRepository
                .findExistingAggregatedNotification(
                        request.getUserId(),
                        request.getType(),
                        request.getEntityId()
                );

        if (existingAggregated.isPresent()) {
            return updateAggregatedNotification(existingAggregated.get(), similarNotifications.size() + 1);
        } else if (!similarNotifications.isEmpty()) {
            int countt = similarNotifications.size() + 1 ;

            similarNotifications.forEach(x -> notificationRepository.delete(x) ) ;

            return createNewAggregatedNotification(request, countt);
        } else {
            Notification notification = buildNotification(request, false);
            Notification savedNotification = notificationRepository.save(notification);
            return convertToResponse(savedNotification);
        }
    }

    private NotificationResponse createNewAggregatedNotification(NotificationRequest request, int count) {
        List<Notification> similarNotifications = notificationRepository
                .findAggregatableNotifications(
                        request.getUserId(),
                        request.getType(),
                        request.getEntityId()
                );

        similarNotifications.forEach(n -> {
            n.setIsAggregated(true);
            notificationRepository.save(n);
        });

        Notification aggregatedNotification = buildNotification(request, true);
        aggregatedNotification.setAggregateCount(count);
        aggregatedNotification.setAggregatedAt(LocalDateTime.now());
        aggregatedNotification.setMessage(generateAggregatedMessage(request.getType(), count, request.getEntityContent()));

        Notification savedNotification = notificationRepository.save(aggregatedNotification);
        return convertToResponse(savedNotification);
    }

    private NotificationResponse updateAggregatedNotification(Notification notification, int newCount) {
        notification.setAggregateCount(newCount);
        notification.setAggregatedAt(LocalDateTime.now());
        notification.setMessage(generateAggregatedMessage(
                notification.getType(),
                newCount,
                notification.getEntityContent()
        ));

        Notification updatedNotification = notificationRepository.save(notification);
        return convertToResponse(updatedNotification);
    }

    private String generateAggregatedMessage(NotificationType type, int count, String entityContent) {
        String baseContent = entityContent != null ?
                (entityContent.length() > 50 ? entityContent.substring(0, 50) + "..." : entityContent) : "";

        switch (type) {
            case POST_LIKE:
                return "someone liked your post: " + baseContent;
            case POST_COMMENT:
                return count + " people commented on your post: " + baseContent;
            case COMMENT_REPLY:
                return count + " people replied to your comment: " + baseContent;
            default:
                return count + " people interacted with your content";
        }
    }

    private Notification buildNotification(NotificationRequest request, boolean isAggregated) {
        String message = request.getCustomMessage();

        if (message == null) {
            message = generateMessage(request);
        }

        return Notification.builder()
                .userId(request.getUserId())
                .senderId(request.getSenderId())
                .type(request.getType())
                .entityId(request.getEntityId())
                .entityContent(request.getEntityContent())
                .message(message)
                .isRead(false)
                .isAggregated(isAggregated)
                .build();
    }

    private String generateMessage(NotificationRequest request) {
        String senderName = "Someone";

        if (request.getSenderId() != null) {
            senderName = userRepository.findById(request.getSenderId())
                    .map(user -> {
                        // تعديل هذا بناءً على حقول الـ User في مشروعك
                        String name = user.getFirstname();
                        if (user.getLastname() != null && !user.getLastname().isEmpty()) {
                            name += " " + user.getLastname();
                        }
                        return name;
                    })
                    .orElse("Someone");
        }

        switch (request.getType()) {
            case PASSWORD_CHANGED:
                return "Your password has been changed successfully";
            case NEW_FOLLOWER:
                return " started following you";
            case POST_LIKE:
                return  " liked your post";
            case POST_COMMENT:
                return  " commented on your post";
            case COMMENT_REPLY:
                return  " replied to your comment";
            default:
                return " You have a new notification";
        }
    }

    public Page<NotificationResponse> getUserNotifications(Integer userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToResponse);
    }

    public Page<NotificationResponse> getUnreadNotifications(Integer userId, Pageable pageable) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToResponse);
    }

    public long getUnreadCount(Integer userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Integer userId) {
        notificationRepository.findById(notificationId)
                .filter(n -> n.getUserId().equals(userId))
                .ifPresent(notification -> {
                    notification.setIsRead(true);
                    notificationRepository.save(notification);
                });
    }

    @Transactional
    public void markAllAsRead(Integer userId) {
        notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, Pageable.unpaged())
                .forEach(notification -> {
                    notification.setIsRead(true);
                    notificationRepository.save(notification);
                });
    }

    private NotificationResponse convertToResponse(Notification notification) {
        String senderName = null;
        String senderAvatar = null;

        if (notification.getSenderId() != null) {
            Optional<User> userOpt = userRepository.findById(notification.getSenderId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                senderName = user.getFirstname();
                if (user.getLastname() != null && !user.getLastname().isEmpty()) {
                    senderName += " " + user.getLastname();
                }
                // إذا كان لديك حقل avatar في الـ User
                // senderAvatar = user.getAvatar();
            }
        }

        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .senderId(notification.getSenderId())
                .senderName(senderName)
                .senderAvatar(senderAvatar)
                .type(notification.getType())
                .entityId(notification.getEntityId())
                .entityContent(notification.getEntityContent())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .isAggregated(notification.getIsAggregated())
                .aggregateCount(notification.getAggregateCount())
                .createdAt(notification.getCreatedAt())
                .aggregatedAt(notification.getAggregatedAt())
                .build();
    }
}