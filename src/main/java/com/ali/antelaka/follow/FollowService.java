package com.ali.antelaka.follow;

import com.ali.antelaka.exceptionHandler.exception.BadRequestException;
import com.ali.antelaka.exceptionHandler.exception.NotFoundException;
import com.ali.antelaka.notification.entity.NotificationRepository;
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
    private final NotificationRepository notificationRepository;
    public Page<User> getFollowers(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Follow> follows = followRepository.findByFollowing(user, pageable);

        return follows.map(Follow::getFollower);
    }

    public Page<User> getFollowing(User user, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Follow> follows = followRepository.findByFollower(user, pageable);
        return follows.map(Follow::getFollowing);

    }

    public Follow followUser(User follower, Integer followingId) {

        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new NotFoundException("Following not found"));

        if (follower.getId().equals(following.getId())) {
            throw new BadRequestException("User cannot follow themselves");
        }
        var x = this.followRepository.findByFollowerAndFollowing(follower , following).orElseGet(()->{
            Follow follow = Follow.builder()
                    .follower(follower)
                    .following(following)
                    .build();
            this.followRepository.save(follow);
            return follow ;
        }) ;



        NotificationRequest request = NotificationRequest.builder()
                .userId(following.getId())
                .senderId(follower.getId())
                .type(NotificationType.NEW_FOLLOWER)
                .entityId(null)
                .entityContent(null)
                .customMessage(null)
                .build();

        notificationService.createNotification(request  , null);


        return null ;
    }


    @Transactional
    public void unfollowUser(User user, Integer followingId) {
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        followRepository.deleteByFollowerAndFollowing(user, following);

        notificationRepository.deleteByUserIdAndSenderIdAndType(
                following.getId(),
                user.getId(),
                NotificationType.NEW_FOLLOWER
        );
    }
}
