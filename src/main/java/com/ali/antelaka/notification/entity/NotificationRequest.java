package com.ali.antelaka.notification.entity;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationRequest {
    private Integer userId;
    private Integer senderId;
    private NotificationType type;
    private Integer entityId;
    private String entityContent;
    private String customMessage;
}