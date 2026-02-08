package com.ali.antelaka.user.service;

import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.auth.AuthenticationService;
import com.ali.antelaka.follow.FollowRepository;
import com.ali.antelaka.google.GoogleUser;

import com.ali.antelaka.notification.entity.NotificationRequest;
import com.ali.antelaka.notification.entity.NotificationType;
import com.ali.antelaka.notification.service.NotificationService;
import com.ali.antelaka.page.entity.PageEntity;
import com.ali.antelaka.page.entity.PageType;
import com.ali.antelaka.page.PageRepository;
import com.ali.antelaka.user.UserRepository;
import com.ali.antelaka.user.dto.UserPublicProfileResponse;
import com.ali.antelaka.user.dto.UserSearchDTO;
import com.ali.antelaka.user.entity.Role;
import com.ali.antelaka.user.entity.User;
import com.ali.antelaka.user.request.ChangePasswordRequest;
import com.ali.antelaka.user.request.UpdateProfileRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository repository;
    private final NotificationService notificationService ;

    @Autowired
    FollowRepository followRepository ;


    @Autowired
    private AuthenticationService authenticationService ;

    @Autowired
    private PageRepository pageRepository ;

    public User findOrCreateUser (GoogleUser googleUser)
    {
        var user =  this.repository.findByEmail(googleUser.getEmail())
                .orElseGet(() -> {
                    User newUser = User.builder()
                        .firstname(extractFirstName(googleUser.getName()))
                        .lastname(extractLastName(googleUser.getName()))
                        .email(googleUser.getEmail())
                        .imagePath(googleUser.getPicture())
                        .password(null)
                        .role(Role.USER)
                        .enabled(true)
                        .build();
            var savedUser = repository.save(newUser);

            PageEntity publicUserPage = PageEntity.builder()
                    .id(savedUser.getId())
                    .user(savedUser)
                    .pageType(PageType.PUBLIC.name())
                    .description("Hi there")
                    .build();
            this.pageRepository.save(publicUserPage);


            return savedUser;
        });
        if (user.isEnabled() ==  false )
        {
            user.setEnabled(true);
            user = repository.save(user) ;
        }
        return user ;
    }

    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "";
        String[] names = fullName.split(" ");
        return names[0];
    }

     private String extractLastName(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "";
        String[] names = fullName.split(" ");
        return names.length > 1 ? names[names.length - 1] : "";
    }



    public ApiResponse changePassword(ChangePasswordRequest request, Principal connectedUser) {

        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        var apiRes =  ApiResponse.builder()
                .success(true)
                .status(HttpStatus.OK.value())
                .build() ;

        if (request.getCurrentPassword() == null )
        {

                if (!LocalDateTime.now().isBefore(user.getMaxTimeToResetPassword())){
                    throw new IllegalStateException("Wrong password");
                }else {
                    apiRes.setMessage("your password has changed !  , please login again");
                    this.authenticationService.revokeAllUserTokens(user);
                }
        }
        else  {
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new IllegalStateException("Wrong password");
            }
            if (!request.getNewPassword().equals(request.getConfirmationPassword())) {
                throw new IllegalStateException("Password are not the same");
            }
            apiRes.setMessage("your password has changed ! ");
        }
        user.setMaxTimeToResetPassword(null);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);


        NotificationRequest notificationReduest = NotificationRequest.builder()
                .userId(user.getId()) // صاحب البوست
                .senderId(user.getId()) // المعلق
                .type(NotificationType.PASSWORD_CHANGED)
                .entityId(null) // معرف البوست
                .entityContent(null) // محتوى البوست
                .customMessage(null) // نص التعليق (اختياري للإشعار)
                .build();

        notificationService.createNotification(notificationReduest);


        return apiRes;
    }

    public UserPublicProfileResponse getUserProfileById(Integer targetUserId, User user) {
         User targetUser = repository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));


        boolean isMyProfile =false ;
        boolean isFollowing = false;

        if (user !=  null ) {
            isMyProfile =  targetUser.getEmail().equals( user.getEmail());
            isFollowing =  false ;
            if (followRepository.findByFollowerAndFollowing(user, targetUser).isPresent())
            {
                isFollowing =  true ;
            }
        }

        return UserPublicProfileResponse.builder()
                .id(targetUser.getId())
                .firstname(targetUser.getFirstname())
                .lastname(targetUser.getLastname())
                .bio(targetUser.getBio())
                .imagePath(targetUser.getImagePath())
                .role(targetUser.getRole().name())
                .email(isMyProfile ? targetUser.getEmail() : null)
                .whatsappLink(targetUser.getWhatsappLink())
                .facebookLink(targetUser.getFacebookLink())
                .telegramLink(targetUser.getTelegramLink())
                .linkedinLink(targetUser.getLinkedinLink())
                .postsCount(targetUser.getPosts() != null ? targetUser.getPosts().size() : 0)
                .followersCount(targetUser.getFollowers() != null ? targetUser.getFollowers().size() : 0)
                .followingCount(targetUser.getFollowing() != null ? targetUser.getFollowing().size() : 0)
                .isMyProfile(isMyProfile)
                .isFollowing(isFollowing)
                .build();
    }

    @Transactional
    public UserPublicProfileResponse updateProfile(User user, UpdateProfileRequest request) {

        if (request.getFirstname() != null) {
            user.setFirstname(request.getFirstname());
        }
        if (request.getLastname() != null) {
            user.setLastname(request.getLastname());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        if (request.getWhatsappLink() != null) {
            user.setWhatsappLink(request.getWhatsappLink());
        }
        if (request.getFacebookLink() != null) {
            user.setFacebookLink(request.getFacebookLink());
        }
        if (request.getTelegramLink() != null) {
            user.setTelegramLink(request.getTelegramLink());
        }
        if (request.getLinkedinLink() != null) {
            user.setLinkedinLink(request.getLinkedinLink());
        }

        User savedUser = repository.save(user);
        return mapToProfileResponse(savedUser);
    }



    private UserPublicProfileResponse mapToProfileResponse(User user) {
        return UserPublicProfileResponse.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .bio(user.getBio())
                .imagePath(user.getImagePath())
                .role(user.getRole().name())
                .whatsappLink(user.getWhatsappLink())
                .facebookLink(user.getFacebookLink())
                .telegramLink(user.getTelegramLink())
                .linkedinLink(user.getLinkedinLink())
                // التعامل الآمن مع القوائم لتجنب NullPointer إذا كانت القائمة فارغة
                .postsCount(user.getPosts() != null ? user.getPosts().size() : 0)
                .followersCount(user.getFollowers() != null ? user.getFollowers().size() : 0)
                .followingCount(user.getFollowing() != null ? user.getFollowing().size() : 0)
                .build();
    }



    public Page<UserSearchDTO> searchUsers(String keyword, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("firstname").ascending());

        Page<User> usersPage = repository.flexibleSearch(keyword, pageable);

        return usersPage.map(this::mapToDTO);
    }

    private UserSearchDTO mapToDTO(User user) {
        return UserSearchDTO.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .imagePath(user.getImagePath())
                .bio(user.getBio())
                .facebookLink(user.getFacebookLink())
                .linkedinLink(user.getLinkedinLink())
                .telegramLink(user.getTelegramLink())
                .whatsappLink(user.getWhatsappLink())
                .build();
    }
}
