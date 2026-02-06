package com.ali.antelaka.notification.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    Optional<NotificationPreference> findByUserId(Integer userId);
    boolean existsByUserId(Integer userId);

    // لحذف token معين (مثلاً عند تسجيل الخروج من جهاز)
    Optional<NotificationPreference> findByFcmToken(String fcmToken);
}