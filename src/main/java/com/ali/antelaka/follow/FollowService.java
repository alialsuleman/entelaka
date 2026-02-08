package com.ali.antelaka.follow;

import com.ali.antelaka.notification.entity.NotificationRequest;
import com.ali.antelaka.notification.entity.NotificationType;
import com.ali.antelaka.notification.service.NotificationService;
import com.ali.antelaka.user.UserRepository;
import com.ali.antelaka.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService ;
    public Page<User> getFollowers(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Follow> follows = followRepository.findByFollowing(user, pageable);

        return follows.map(Follow::getFollower); // نرجع فقط المستخدمين
    }

    public Page<User> getFollowing(User user, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Follow> follows = followRepository.findByFollower(user, pageable);
        return follows.map(Follow::getFollowing); // نرجع فقط المستخدمين

    }

    public Follow followUser(User follower, Integer followingId) {

        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("Following not found"));

        if (follower.getId().equals(following.getId())) {
            throw new RuntimeException("User cannot follow themselves");
        }
        var x = this.followRepository.findByFollowerAndFollowing(follower , following).orElseGet(()->{
            Follow follow = Follow.builder()
                    .follower(follower)
                    .following(following)
                    .build();
            this.followRepository.save(follow);
            System.out.println("added");
            return follow ;
        }) ;



        NotificationRequest request = NotificationRequest.builder()
                .userId(following.getId()) // صاحب البوست
                .senderId(follower.getId()) // المعلق
                .type(NotificationType.NEW_FOLLOWER)
                .entityId(null) // معرف البوست
                .entityContent(null) // محتوى البوست
                .customMessage(null) // نص التعليق (اختياري للإشعار)
                .build();

        notificationService.createNotification(request);


        return null ;
    }


    @Transactional
    public void unfollowUser(User user, Integer followingId) {
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        followRepository.deleteByFollowerAndFollowing(user, following);
    }
}
