package com.ali.antelaka.admin;


import com.ali.antelaka.admin.*;
import com.ali.antelaka.post.repository.CommentRepository;
import com.ali.antelaka.post.entity.Comment;
import com.ali.antelaka.post.entity.Post;
import com.ali.antelaka.post.entity.PostImage;
import com.ali.antelaka.post.repository.PostRepository;
import com.ali.antelaka.user.entity.User;
import com.ali.antelaka.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminPostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<AdminPostResponseDTO> getAllPosts(AdminPostSearchRequest request) {
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 10;
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "createdAt";
        String sortDirection = request.getSortDirection() != null ? request.getSortDirection() : "DESC";

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Post> postsPage = postRepository.findAllForAdmin(
                request.getKeyword(),
                request.getTag(),
                pageable
        );

        return postsPage.map(this::convertToPostDTO);
    }

    @Transactional(readOnly = true)
    public AdminPostResponseDTO getPostDetails(Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        return convertToPostDTO(post);
    }

    @Transactional(readOnly = true)
    public Page<AdminCommentResponseDTO> getCommentsByPostId(Integer postId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // تحقق من وجود البوست
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("Post not found with id: " + postId);
        }

        Page<Comment> commentsPage = commentRepository.findCommentsByPostIdForAdmin(postId, keyword, pageable);
        return commentsPage.map(this::convertToCommentDTO);
    }

    @Transactional(readOnly = true)
    public Page<AdminCommentResponseDTO> getAllComments(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Comment> commentsPage = commentRepository.findAllCommentsForAdmin(keyword, pageable);
        return commentsPage.map(this::convertToCommentDTO);
    }

    @Transactional(readOnly = true)
    public AdminCommentResponseDTO getCommentDetails(Integer commentId) {
        Comment comment = commentRepository.findCommentWithUser(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
        return convertToCommentDTO(comment);
    }

    @Transactional
    public void deletePost(Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        log.info("Deleting post with id: {} by admin", postId);
        postRepository.delete(post);
    }

    @Transactional
    public void deleteComment(Integer commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        // تحديث عدد التعليقات في البوست
        Post post = comment.getPost();
        if (post != null) {
            post.setNumberOfComment(Math.max(0, post.getNumberOfComment() - 1));
            postRepository.save(post);
        }

        // إذا كان التعليق رد على تعليق آخر، قم بتحديث عدد الردود
        if (comment.getCommentParent() != null) {
            Comment parentComment = comment.getCommentParent();
            parentComment.setNumberOfSubComment(Math.max(0, parentComment.getNumberOfSubComment() - 1));
            commentRepository.save(parentComment);
        }

        log.info("Deleting comment with id: {} by admin", commentId);
        commentRepository.delete(comment);
    }

    @Transactional
    public void deleteAllUserPosts(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        List<Post> userPosts = postRepository.findByUser(user, Pageable.unpaged()).getContent();
        log.info("Deleting {} posts for user id: {} by admin", userPosts.size(), userId);
        postRepository.deleteAll(userPosts);
    }

    @Transactional
    public void deleteAllUserComments(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        List<Comment> userComments = commentRepository.findByUser_IdOrderByCreatedAtDesc(userId, Pageable.unpaged()).getContent();
        log.info("Deleting {} comments for user id: {} by admin", userComments.size(), userId);
        commentRepository.deleteAll(userComments);
    }

    private AdminPostResponseDTO convertToPostDTO(Post post) {
        User user = post.getUser();

        List<String> imageUrls = post.getPostImages() != null
                ? post.getPostImages().stream()
                .map(PostImage::getImageUrl) // افترض أن لديك حقل imagePath
                .collect(java.util.stream.Collectors.toList())
                : java.util.Collections.emptyList();

        return AdminPostResponseDTO.builder()
                .id(post.getId())
                .text(post.getText())
                .tag(post.getTag())
                .numberOfLikes(post.getNumberOfLikes())
                .numberOfComment(post.getNumberOfComment())
                .isPublic(post.getIsPublic())
                .isUpdated(post.isUpdated())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .userId(user != null ? user.getId() : null)
                .userFirstname(user != null ? user.getFirstname() : null)
                .userLastname(user != null ? user.getLastname() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .userImagePath(user != null ? user.getImagePath() : null)
                .pageId(post.getPageEntity() != null ? post.getPageEntity().getId() : null)
                .postImageUrls(imageUrls)
                .userEnabled(user != null && user.isEnabled())
                .build();
    }

    private AdminCommentResponseDTO convertToCommentDTO(Comment comment) {
        User user = comment.getUser();
        Post post = comment.getPost();

        return AdminCommentResponseDTO.builder()
                .id(comment.getId())
                .text(comment.getText())
                .numberOfLikes(comment.getNumberOfLikes())
                .numberOfSubComment(comment.getNumberOfSubComment())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .userId(user != null ? user.getId() : null)
                .userFirstname(user != null ? user.getFirstname() : null)
                .userLastname(user != null ? user.getLastname() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .userImagePath(user != null ? user.getImagePath() : null)
                .postId(post != null ? post.getId() : null)
                .postText(post != null ? post.getText() : null)
                .commentParentId(comment.getCommentParent() != null ? comment.getCommentParent().getId() : null)
                .repliedUserId(comment.getRepliedUserId())
                .repliedUsername(comment.getRepliedUsername())
                .userEnabled(user != null && user.isEnabled())
                .build();
    }
}