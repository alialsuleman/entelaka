package com.ali.antelaka.post.repository;

import com.ali.antelaka.post.entity.LikeEntity;
import com.ali.antelaka.post.entity.SaveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SaveRepository extends JpaRepository<SaveEntity, Integer> {
    Optional<SaveEntity> findByUserIdAndPostId(Integer userId, Integer postId);
}