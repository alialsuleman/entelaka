package com.ali.antelaka.admin;



import com.ali.antelaka.follow.FollowRepository;
import com.ali.antelaka.post.repository.PostRepository;
import com.ali.antelaka.user.UserRepository;
import com.ali.antelaka.user.entity.Role;
import com.ali.antelaka.user.entity.User;
 import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public Page<AdminUserResponseDTO> getAllUsers(AdminUserSearchRequest request) {
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 10;
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "id";
        String sortDirection = request.getSortDirection() != null ? request.getSortDirection() : "DESC";

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> usersPage;

        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            usersPage = userRepository.flexibleSearch(request.getKeyword(), pageable);
        } else {
            usersPage = userRepository.findAll(pageable);
        }

        return usersPage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<AdminUserResponseDTO> searchUsersByKeyword(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<User> usersPage = userRepository.flexibleSearch(keyword, pageable);
        return usersPage.map(this::convertToDTO);
    }

    @Transactional
    public User banUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        if (user.getRole()!= Role.USER) throw new RuntimeException("you are not allowed to do that");
        user.setEnabled(false);
        return userRepository.save(user);
    }

    @Transactional
    public User unbanUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        user.setEnabled(true);
        return userRepository.save(user);
    }

    @Transactional
    public User toggleUserBan(Integer userId, boolean banned) {
        if (banned) {
            return banUser(userId);
        } else {
            return unbanUser(userId);
        }
    }

    @Transactional(readOnly = true)
    public AdminUserResponseDTO getUserDetails(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return convertToDTO(user);
    }


    private AdminUserResponseDTO convertToDTO(User user) {
        int postsCount = postRepository.countByUser(user);
        int followersCount = followRepository.countByFollowing(user);
        int followingCount = followRepository.countByFollower(user);

        return AdminUserResponseDTO.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .imagePath(user.getImagePath())
                .role(user.getRole() != null ? user.getRole().name() : "USER")
                .bio(user.getBio())
                .enabled(user.isEnabled())
                .createdAt(null) // You may need to add createdAt field to User entity
                .postsCount(postsCount)
                .followersCount(followersCount)
                .followingCount(followingCount)
                .build();
    }
}