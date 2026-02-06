package com.ali.antelaka.notification.entity;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    Page<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Integer userId, Pageable pageable);

    long countByUserIdAndIsReadFalse(Integer userId);

    Optional<Notification> findByNotificationCode(String notificationCode);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId " +
            "AND n.type = :type " +
            "AND n.entityId = :entityId ")
    List<Notification> findAggregatableNotifications(
            @Param("userId") Integer userId,
            @Param("type") NotificationType type,
            @Param("entityId") Integer entityId);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId " +
            "AND n.type = :type " +
            "AND n.entityId = :entityId " +
            "AND n.isAggregated = true " +
            "ORDER BY n.aggregatedAt DESC")
    Optional<Notification> findExistingAggregatedNotification(
            @Param("userId") Integer userId,
            @Param("type") NotificationType type,
            @Param("entityId") Integer entityId);
}