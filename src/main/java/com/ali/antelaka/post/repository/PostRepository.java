package com.ali.antelaka.post.repository;

import com.ali.antelaka.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    @Query("SELECT p FROM Post p " +
            "WHERE p.isPublic = true " +
            "AND p.updatedAt < :x " +
            "ORDER BY p.updatedAt DESC")
    List<Post> findOlderPublicPosts(@Param("x") LocalDateTime x, Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "WHERE p.isPublic = true " +
            "AND p.updatedAt > :x " +
            "ORDER BY p.updatedAt ASC")
    List<Post> findNewerPublicPosts(@Param("x") LocalDateTime x, Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "JOIN p.user u " +
            "JOIN Follow f ON f.following = u " +
            "WHERE f.follower.id = :followerId " +
            "AND p.updatedAt < :x " +
            "ORDER BY p.updatedAt DESC")
    List<Post> findOlderPosts(
            @Param("followerId") Integer followerId,
            @Param("x") LocalDateTime x,
            Pageable pageable);


    @Query("SELECT p FROM Post p " +
            "JOIN p.user u " +
            "JOIN Follow f ON f.following = u " +
            "WHERE f.follower.id = :followerId " +
            "AND p.updatedAt > :x " +
            "ORDER BY p.updatedAt ASC")
    List<Post> findNewerPosts(
            @Param("followerId") Integer followerId,
            @Param("x") LocalDateTime x, Pageable pageable);


//    @Query("SELECT p FROM Post p " +
//            "WHERE p.user.id IN (SELECT f.following.id FROM Follow f WHERE f.follower.id = :followerId) " +
//            "AND p.updatedAt < :x " +
//            "ORDER BY p.updatedAt DESC")
//    List<Post> findOlderPosts(@Param("followerId") Integer followerId,
//                              @Param("x") LocalDateTime x,
//                              Pageable pageable);
//
//    @Query("SELECT p FROM Post p " +
//            "WHERE p.user.id IN (SELECT f.following.id FROM Follow f WHERE f.follower.id = :followerId) " +
//            "AND p.updatedAt >= :x " +
//            "ORDER BY p.updatedAt ASC")
//    List<Post> findNewerPosts(@Param("followerId") Integer followerId,
//                              @Param("x") LocalDateTime x,
//                              Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "WHERE p.user.id IN (" +
            "   SELECT f.following.id FROM Follow f WHERE f.follower.id = :followerId" +
            ") ORDER BY p.updatedAt DESC")
    List<Post> findAllFollowingsPosts(@Param("followerId") Long followerId);


    @Query("SELECT p FROM Post p " +
            "JOIN p.user u " +
            "JOIN Follow f ON f.following = u " +
            "WHERE f.follower.id = :followerId " +
            "AND f.following.id IN (" +
            "    SELECT ff.following.id FROM Follow ff " +
            "    WHERE ff.follower.id = :followerId " +
            "    ORDER BY ff.createdAt DESC " +
            "    LIMIT 10" +
            ") " +
            "AND p.updatedAt < :x " +
            "ORDER BY p.updatedAt DESC")
    List<Post> findOlderFollowedPosts (@Param("followerId") Long followerId,
                                      @Param("x") LocalDateTime x,
                                      Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "JOIN p.user u " +
            "JOIN Follow f ON f.following = u " +
            "WHERE f.follower.id = :followerId " +
            "AND f.following.id IN (" +
            "    SELECT ff.following.id FROM Follow ff " +
            "    WHERE ff.follower.id = :followerId " +
            "    ORDER BY ff.createdAt DESC " +
            "    LIMIT 10" +
            ") " +
            "AND p.updatedAt >= :x " +
            "ORDER BY p.updatedAt ASC")
    List<Post> findNewerFollowedPosts(@Param("followerId") Long followerId,
                                      @Param("x") LocalDateTime x,
                                      Pageable pageable);
}