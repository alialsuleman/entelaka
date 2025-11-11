package com.ali.antelaka.post.repository;

import com.ali.antelaka.post.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository  extends JpaRepository<Comment, Integer> {

    Page<Comment> findByPostId(Integer postId, Pageable pageable);

    Page<Comment> findByPostIdOrderByCreatedAtDesc(Integer postId, Pageable pageable);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.post.id = :postId")
    Page<Comment> findByPostIdWithUser(@Param("postId") Integer postId, Pageable pageable);

    void deleteById(Integer id);

}