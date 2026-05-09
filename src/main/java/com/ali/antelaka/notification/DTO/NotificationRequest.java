package com.ali.antelaka.notification.DTO;


import com.ali.antelaka.notification.entity.NotificationType;
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