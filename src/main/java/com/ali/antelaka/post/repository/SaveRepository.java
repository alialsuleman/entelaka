package com.ali.antelaka.post.repository;

import com.ali.antelaka.post.entity.LikeEntity;
import com.ali.antelaka.post.entity.SaveEntity;
import com.ali.antelaka.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaveRepository extends JpaRepository<SaveEntity, Integer> {
    Optional<SaveEntity> findByUserIdAndPostId(Integer userId, Integer postId);
    Page<SaveEntity> findByUser(User user, Pageable pageable);

}