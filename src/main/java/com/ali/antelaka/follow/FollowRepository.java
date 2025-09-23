package com.ali.antelaka.follow;

import com.ali.antelaka.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Integer> {

    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    Optional<Follow> findByFollower_IdAndFollowing_Id(Integer followerId, Integer followingId);
    // جلب الناس اللي بيتابعهم المستخدم
    Page<Follow> findByFollower(User follower, Pageable pageable);
    List<Follow> findByFollower(User follower);

    // جلب الناس اللي بيتابعوا المستخدم
    Page<Follow> findByFollowing(User following, Pageable pageable);
    List<Follow> findByFollowing(User following );

}
