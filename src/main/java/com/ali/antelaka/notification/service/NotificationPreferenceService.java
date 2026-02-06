package com.ali.antelaka.notification.service;

import com.ali.antelaka.notification.entity.NotificationPreference;
import com.ali.antelaka.notification.entity.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;

    @Transactional
    public void saveFcmToken(Integer userId, String fcmToken) {
        var preferenceOptional = preferenceRepository.findByUserId(userId);

        NotificationPreference preference;
        if (preferenceOptional.isPresent()) {
            preference = preferenceOptional.get();
            preference.setFcmToken(fcmToken);
        } else {
            preference = NotificationPreference.builder()
                    .userId(userId)
                    .fcmToken(fcmToken)
                    .pushEnabled(true)
                    .emailEnabled(true)
                    .build();
        }

        preferenceRepository.save(preference);
    }

    @Transactional
    public void updatePushEnabled(Integer userId, boolean enabled) {
        var preferenceOptional = preferenceRepository.findByUserId(userId);

        if (preferenceOptional.isPresent()) {
            NotificationPreference preference = preferenceOptional.get();
            preference.setPushEnabled(enabled);
            preferenceRepository.save(preference);
        }
    }

    @Transactional
    public void updateEmailEnabled(Integer userId, boolean enabled) {
        var preferenceOptional = preferenceRepository.findByUserId(userId);

        if (preferenceOptional.isPresent()) {
            NotificationPreference preference = preferenceOptional.get();
            preference.setEmailEnabled(enabled);
            preferenceRepository.save(preference);
        }
    }

    @Transactional
    public void removeFcmToken(Integer userId) {
        var preferenceOptional = preferenceRepository.findByUserId(userId);

        if (preferenceOptional.isPresent()) {
            NotificationPreference preference = preferenceOptional.get();
            preference.setFcmToken(null);
            preferenceRepository.save(preference);
        }
    }
}