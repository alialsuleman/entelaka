
package com.ali.antelaka.post.service;

import com.ali.antelaka.follow.FollowRepository;
import com.ali.antelaka.page.PageRepository;
import com.ali.antelaka.page.entity.PageEntity;
import com.ali.antelaka.post.DTO.PostDTO;
import com.ali.antelaka.post.entity.Post;
import com.ali.antelaka.post.entity.PostImage;
import com.ali.antelaka.post.repository.LikeRepository;
import com.ali.antelaka.post.repository.*;
import com.ali.antelaka.post.request.CreatePostRequest;
import com.ali.antelaka.user.UserRepository;
import com.ali.antelaka.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PageRepository pageRepository;
    private final PostImageRepository postImageRepository;
    private final LikeRepository likeRepository;
    private final SaveRepository saveRepository;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public Optional<Post> getPostById(Integer postId) {
        return postRepository.findById(postId);
    }

    public Post createPost(User user, Integer pageId, CreatePostRequest createPostRequest) {
        PageEntity pageEntity = pageRepository.findById(pageId)
                .orElseThrow(() -> new RuntimeException("Page not exist"));

        Post post = Post.builder()
                .tag(createPostRequest.getTag())
                .text(createPostRequest.getText())
                .user(user)
                .pageEntity(pageEntity)
                .build();

        Post savedPost = postRepository.save(post);

        if (createPostRequest.getPostImageIds() != null) {
            List<PostImage> postImgs = new ArrayList<>();
            createPostRequest.getPostImageIds().forEach(id -> {
                PostImage img = postImageRepository.findById(id).get();
                if (img.getPost() != null) throw new RuntimeException("unvalid image selected");
                img.setPost(savedPost);
                postImgs.add(img);
                postImageRepository.save(img);
            });
            savedPost.setPostImages(postImgs);
        }

        return postRepository.save(savedPost);
    }

    public Page<PostDTO> getPostsByUser(Integer userId, User currentUser, Pageable pageable) {
        User postOwner = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return postRepository.findByUser(postOwner, pageable)
                .map(post -> new PostDTO(post, currentUser, likeRepository, saveRepository, followRepository));
    }

    public boolean deletePost(User user, Integer postId) {
        var o_post = postRepository.findById(postId);
        if (!o_post.isPresent()) return false;

        Post post = o_post.get();
        if (!post.getUser().getId().equals(user.getId())) return false;

        postRepository.delete(post);
        return true;
    }

    public PostDTO updatePost(User user, Integer postId, CreatePostRequest updateRequest) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You do not have permission to update this post.");
        }

        if (updateRequest.getText() != null) post.setText(updateRequest.getText());
        if (updateRequest.getTag()  != null) post.setTag(updateRequest.getTag());
        post.setUpdatedAt(LocalDateTime.now());

        if (updateRequest.getPostImageIds() != null) {
            List<PostImage> newImages = new ArrayList<>();
            for (Integer id : updateRequest.getPostImageIds()) {
                PostImage img = postImageRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Image not found"));
                if (img.getPost() != null && !img.getPost().getId().equals(post.getId())) {
                    throw new RuntimeException("Image already used in another post");
                }
                img.setPost(post);
                newImages.add(img);
            }
            post.getPostImages().clear();
            post.getPostImages().addAll(newImages);
        }

        Post saved = postRepository.save(post);
        return new PostDTO(saved, user, likeRepository, saveRepository, followRepository);
    }

    public List<PostDTO> searchPosts(String tag, String searchText, Pageable pageable, User user) {
        return postRepository.searchPosts(tag, searchText, pageable)
                .stream()
                .map(post -> new PostDTO(post, user, likeRepository, saveRepository, followRepository))
                .toList();
    }

    public List<PostDTO> getOlderPosts(Integer userId, LocalDateTime x, int limit, boolean onlyPublic, User user, String tag) {
        if (x == null) x = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, limit);

        List<Post> posts = onlyPublic
                ? postRepository.findOlderPublicPosts(x, tag, pageable)
                : postRepository.findOlderPosts(userId, x, tag, pageable);

        return posts.stream()
                .map(post -> new PostDTO(post, user, likeRepository, saveRepository, followRepository))
                .toList();
    }

    public List<PostDTO> getNewerPosts(Integer userId, LocalDateTime x, int limit, boolean onlyPublic, User user, String tag) {
        if (x == null) x = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, limit);

        List<Post> posts = onlyPublic
                ? postRepository.findNewerPublicPosts(x, tag, pageable)
                : postRepository.findNewerPosts(userId, x, tag, pageable);

        return posts.stream()
                .map(post -> new PostDTO(post, user, likeRepository, saveRepository, followRepository))
                .toList();
    }
}