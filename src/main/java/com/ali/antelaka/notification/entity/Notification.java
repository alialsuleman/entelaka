package com.ali.antelaka.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "sender_id")
    private Integer senderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(name = "entity_id")
    private Integer entityId;

    @Column(name = "entity_content", length = 500)
    private String entityContent;

    @Column(nullable = false)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "is_aggregated", nullable = false)
    private boolean isAggregated;

    @Column(name = "aggregate_count")
    private Integer aggregateCount;

    @Column(name = "notification_code", nullable = false, unique = true)
    private String notificationCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "aggregated_at")
    private LocalDateTime aggregatedAt;

    // Getters and Setters يدوياً لحقول الـ boolean
    public boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public boolean getIsAggregated() {
        return isAggregated;
    }

    public void setIsAggregated(boolean isAggregated) {
        this.isAggregated = isAggregated;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (aggregateCount == null) {
            aggregateCount = 1;
        }
        if (notificationCode == null) {
            notificationCode = generateNotificationCode();
        }
    }

    private String generateNotificationCode() {
        return "NOTIF_" + System.currentTimeMillis() + "_" + this.hashCode();
    }
}
