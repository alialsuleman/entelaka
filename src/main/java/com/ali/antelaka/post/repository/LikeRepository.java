package com.ali.antelaka.post.repository;

import com.ali.antelaka.post.entity.LikeEntity;
import com.ali.antelaka.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<LikeEntity, Integer> {
    Optional<LikeEntity> findByUserIdAndPostId(Integer userId, Integer postId);
}