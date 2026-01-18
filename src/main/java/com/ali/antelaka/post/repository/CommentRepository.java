package com.ali.antelaka.post.repository;

import com.ali.antelaka.post.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository  extends JpaRepository<Comment, Integer> {


    Page<Comment> findByPostIdAndCommentParentIsNullOrderByCreatedAtDesc(Integer postId, Pageable pageable);

    Page<Comment> findByCommentParentIdOrderByCreatedAtAsc(Integer commentParentId, Pageable pageable);

    Page<Comment> findByPostId(Integer postId, Pageable pageable);

    Page<Comment> findByPostIdOrderByCreatedAtDesc(Integer postId, Pageable pageable);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.post.id = :postId")
    Page<Comment> findByPostIdWithUser(@Param("postId") Integer postId, Pageable pageable);

    void deleteById(Integer id);

    Page<Comment> findByUser_IdOrderByCreatedAtDesc(Integer userId, Pageable pageable);


    @Query("SELECT c FROM Comment c WHERE c.id = :commentId")
    Optional<Comment> findCommentById(@Param("commentId") Integer commentId);

    @Query("SELECT c FROM Comment c " +
            "LEFT JOIN FETCH c.user " +
            "LEFT JOIN FETCH c.post p " +
            "LEFT JOIN FETCH p.user " +
            "WHERE c.id = :commentId")
    Optional<Comment> findCommentWithDetails(@Param("commentId") Integer commentId);



    // استعلام لجلب تعليقات متعددة مع تفاصيلهم
    @Query("SELECT c FROM Comment c " +
            "LEFT JOIN FETCH c.user " +
            "LEFT JOIN FETCH c.post p " +
            "LEFT JOIN FETCH p.user " +
            "WHERE c.id IN :commentIds")
    List<Comment> findCommentsWithDetails(@Param("commentIds") List<Integer> commentIds);



}