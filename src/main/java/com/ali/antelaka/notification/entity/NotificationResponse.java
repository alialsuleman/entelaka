package com.ali.antelaka.notification.entity;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private Integer userId;
    private Integer senderId;
    private String senderName;
    private String senderAvatar;
    private NotificationType type;
    private Integer entityId;
    private String entityContent;
    private String message;
    private boolean isRead;
    private boolean isAggregated;
    private Integer aggregateCount;
    private LocalDateTime createdAt;
    private LocalDateTime aggregatedAt;
}