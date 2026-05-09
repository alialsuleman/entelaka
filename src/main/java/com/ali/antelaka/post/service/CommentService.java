package com.ali.antelaka.post.service;

import com.ali.antelaka.follow.FollowRepository;
import com.ali.antelaka.notification.DTO.NotificationRequest;
import com.ali.antelaka.notification.entity.NotificationType;
import com.ali.antelaka.notification.service.NotificationService;
import com.ali.antelaka.post.DTO.*;
import com.ali.antelaka.post.entity.Comment;
import com.ali.antelaka.post.entity.Post;
import com.ali.antelaka.post.repository.LikeOnCommentRepository;
import com.ali.antelaka.post.repository.CommentRepository;
import com.ali.antelaka.post.repository.PostRepository;
import com.ali.antelaka.post.request.CreateCommentRequest;
import com.ali.antelaka.user.UserRepository;
import com.ali.antelaka.user.entity.Role;
import com.ali.antelaka.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeOnCommentRepository likeOnCommentRepository;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public CommentDTO createComment(User user, Integer postId, Integer commentParentId,
                                    Integer repliedUserId, CreateCommentRequest req) {
        var o_post = postRepository.findById(postId);
        if (!o_post.isPresent()) return null;

        Post post = o_post.get();
        Comment parentComment = null;

        if (commentParentId != null) {
            var o_comment = commentRepository.findById(commentParentId);
            if (!o_comment.isPresent()) return null;
            parentComment = o_comment.get();
            parentComment.setNumberOfSubComment(parentComment.getNumberOfSubComment() + 1);
            commentRepository.save(parentComment);
        }

        String repliedUsername = resolveRepliedUsername(repliedUserId);

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .commentParent(parentComment)
                .text(req.getText())
                .repliedUserId(repliedUserId)
                .repliedUsername(repliedUsername)
                .build();

        commentRepository.save(comment);

        post.setNumberOfComment(post.getNumberOfComment() + 1);
        postRepository.save(post);

        if (!post.getUser().getId().equals(user.getId())) {
            notificationService.createNotification(NotificationRequest.builder()
                    .userId(post.getUser().getId())
                    .senderId(user.getId())
                    .type(NotificationType.POST_COMMENT)
                    .entityId(post.getId())
                    .entityContent(post.getText())
                    .customMessage(comment.getText())
                    .build(), null);
        }

        return new CommentDTO(comment);
    }

    public CommentDTO editComment(Integer commentId, String newText, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(user.getId()) &&
                !user.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("You are not allowed to edit this comment");
        }

        comment.setText(newText);
        return new CommentDTO(commentRepository.save(comment), user, likeOnCommentRepository, followRepository);
    }

    public boolean deleteComment(Integer commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (user.getRole() == Role.USER && !comment.getUser().getId().equals(user.getId())) {
            return false;
        }

        if (comment.getCommentParent() != null) {
            comment.getCommentParent().setNumberOfSubComment(
                    comment.getCommentParent().getNumberOfSubComment() - 1);
            commentRepository.save(comment.getCommentParent());
        }

        commentRepository.delete(comment);
        return true;
    }

    public List<CommentDTO> getCommentsByPostIdWithUserInfo(Integer postId, Pageable pageable, User user) {
        return commentRepository
                .findByPostIdAndCommentParentIsNullOrderByCreatedAtDesc(postId, pageable)
                .map(comment -> new CommentDTO(comment, user, likeOnCommentRepository, followRepository))
                .toList();
    }

    public List<CommentDTO> getRepliesOnCommentsByCommentIdWithUserInfo(Integer commentId, Pageable pageable, User user) {
        return commentRepository
                .findByCommentParentIdOrderByCreatedAtAsc(commentId, pageable)
                .map(comment -> new CommentDTO(comment, user, likeOnCommentRepository, followRepository))
                .toList();
    }

    public Page<CommentHistoryDTO> getUserCommentHistory(Integer userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Comment> userComments = commentRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);

        if (userComments.isEmpty()) return Page.empty(pageable);

        List<CommentHistoryDTO> dtos = userComments.getContent()
                .stream()
                .map(comment -> buildHistoryItem(comment, userId))
                .toList();

        return new PageImpl<>(dtos, pageable, userComments.getTotalElements());
    }

    // ─────────────────────────────────────────────
    //  Private helpers
    // ─────────────────────────────────────────────

    private String resolveRepliedUsername(Integer repliedUserId) {
        if (repliedUserId == null || repliedUserId == 0) return "";

        return userRepository.findById(repliedUserId)
                .map(u -> Stream.of(u.getFirstname(), u.getLastname())
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" ")))
                .orElse("");
    }

    private CommentHistoryDTO buildHistoryItem(Comment userComment, Integer userId) {
        List<Comment> ancestors = buildCommentBranch(userComment);

        List<CommentAncestorDTO> ancestorDTOs = IntStream
                .range(0, ancestors.size())
                .mapToObj(i -> convertToAncestorDTO(ancestors.get(i), i + 1))
                .toList();

        return CommentHistoryDTO.builder()
                .post(convertToPostSummaryDTO(userComment.getPost()))
                .myComment(convertToMyCommentDTO(userComment, ancestors.size() + 1))
                .ancestors(ancestorDTOs)
                .hasDeeperParent(!ancestors.isEmpty())
                .build();
    }

    private List<Comment> buildCommentBranch(Comment comment) {
        List<Comment> ancestors = new ArrayList<>();
        Comment current = comment.getCommentParent();
        while (current != null) {
            ancestors.add(0, getCommentWithDetails(current.getId()));
            current = current.getCommentParent();
        }
        return ancestors;
    }

    private Comment getCommentWithDetails(Integer commentId) {
        if (commentId == null || commentId == 0) return null;
        return commentRepository.findCommentWithDetails(commentId).orElse(null);
    }

    private CommentAncestorDTO convertToAncestorDTO(Comment comment, int uiLevel) {
        if (comment == null || comment.getUser() == null) return null;

        User user = comment.getUser();
        String name = Stream.of(user.getFirstname(), user.getLastname())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));

        return CommentAncestorDTO.builder()
                .id(comment.getId())
                .uiLevel(uiLevel)
                .text(comment.getText())
                .createdAt(comment.getCreatedAt())
                .owner(UserInfoDTO.builder()
                        .userId(user.getId())
                        .username(name)
                        .userImagePath(user.getImagePath())
                        .me(false)
                        .iFollowingHim(false)
                        .build())
                .build();
    }

    private PostSummaryDTO convertToPostSummaryDTO(Post post) {
        if (post == null) return null;

        String text = post.getText();
        String snippet = (text != null && text.length() > 50)
                ? text.substring(0, 50) + "..."
                : text;

        User publisher = post.getUser();
        return PostSummaryDTO.builder()
                .id(post.getId())
                .textSnippet(snippet)
                .publisherName(publisher != null ? publisher.getUsername() : "Unknown")
                .publisherAvatar(publisher != null ? publisher.getImagePath() : null)
                .build();
    }

    private MyCommentDTO convertToMyCommentDTO(Comment comment, int uiLevel) {
        return MyCommentDTO.builder()
                .id(comment.getId())
                .text(comment.getText())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .likes(comment.getNumberOfLikes())
                .replies(comment.getNumberOfSubComment())
                .uiLevel(uiLevel)
                .replyToCommentId(comment.getCommentParent() != null
                        ? comment.getCommentParent().getId()
                        : null)
                .build();
    }
}