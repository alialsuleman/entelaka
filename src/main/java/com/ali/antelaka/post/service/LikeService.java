package com.ali.antelaka.post.service;

import com.ali.antelaka.notification.entity.NotificationRepository;
import com.ali.antelaka.notification.DTO.NotificationRequest;
import com.ali.antelaka.notification.entity.NotificationType;
import com.ali.antelaka.notification.service.NotificationService;
import com.ali.antelaka.post.DTO.PostDTO;
import com.ali.antelaka.post.entity.Post;
import com.ali.antelaka.post.entity.SaveEntity;
import com.ali.antelaka.post.entity.LikeEntity;
import com.ali.antelaka.post.entity.LikeOnComment;
import com.ali.antelaka.post.repository.LikeOnCommentRepository;
import com.ali.antelaka.post.repository.LikeRepository;
import com.ali.antelaka.post.repository.CommentRepository;
import com.ali.antelaka.post.entity.Comment;
import com.ali.antelaka.post.repository.PostRepository;
import com.ali.antelaka.post.repository.SaveRepository;
import com.ali.antelaka.follow.FollowRepository;
import com.ali.antelaka.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final LikeOnCommentRepository likeOnCommentRepository;
    private final SaveRepository saveRepository;
    private final FollowRepository followRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    public boolean flipLike(User user, Integer postId) {
        var o_post = postRepository.findById(postId);
        if (!o_post.isPresent()) return false;

        Post post = o_post.get();
        var is_added = likeRepository.findByUserIdAndPostId(user.getId(), postId);

        if (is_added.isPresent()) {
            likeRepository.delete(is_added.get());
            post.setNumberOfLikes(post.getNumberOfLikes() - 1);
            postRepository.save(post);

            notificationRepository.deleteByUserIdAndSenderIdAndTypeAndEntityId(
                    post.getUser().getId(),
                    user.getId(),
                    NotificationType.POST_LIKE,
                    postId
            );
        } else {
            likeRepository.save(LikeEntity.builder().post(post).user(user).build());
            post.setNumberOfLikes(post.getNumberOfLikes() + 1);
            postRepository.save(post);

            if (!post.getUser().getId().equals(user.getId())) {
                notificationService.createNotification(NotificationRequest.builder()
                        .userId(post.getUser().getId())
                        .senderId(user.getId())
                        .type(NotificationType.POST_LIKE)
                        .entityId(postId)
                        .entityContent(post.getText())
                        .build(), null);
            }
        }
        return true;
    }

    public boolean flipLikeOnComment(User user, Integer commentId) {
        var o_comment = commentRepository.findById(commentId);
        if (!o_comment.isPresent()) return false;

        Comment comment = o_comment.get();
        var is_added = likeOnCommentRepository.findByUserIdAndCommentId(user.getId(), commentId);

        if (is_added.isPresent()) {
            likeOnCommentRepository.delete(is_added.get());
            comment.setNumberOfLikes(comment.getNumberOfLikes() - 1);
        } else {
            likeOnCommentRepository.save(LikeOnComment.builder().comment(comment).user(user).build());
            comment.setNumberOfLikes(comment.getNumberOfLikes() + 1);
        }

        commentRepository.save(comment);
        return true;
    }

    public int flipSave(User user, Integer postId, boolean isPublic) {
        var o_post = postRepository.findById(postId);
        if (!o_post.isPresent()) return 0;

        Post post = o_post.get();
        var is_added = saveRepository.findByUserIdAndPostId(user.getId(), postId);

        if (is_added.isPresent()) {
            saveRepository.delete(is_added.get());
            return 1;
        }

        saveRepository.save(SaveEntity.builder().post(post).user(user).isPublic(isPublic).build());
        return 2;
    }

    public Page<PostDTO> getSavedPosts(User user, Pageable pageable) {
        return saveRepository.findByUser(user, pageable)
                .map(save -> new PostDTO(save.getPost(), user, likeRepository, saveRepository, followRepository));
    }
}