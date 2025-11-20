package com.ali.antelaka.post.repository;


import com.ali.antelaka.post.entity.LikeEntity;
import com.ali.antelaka.post.entity.LikeOnComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeOnCommentRepository extends JpaRepository<LikeOnComment, Integer> {
    Optional<LikeOnComment> findByUserIdAndCommentId(Integer userId, Integer postId);

}
