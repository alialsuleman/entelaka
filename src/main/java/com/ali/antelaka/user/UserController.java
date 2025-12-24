package com.ali.antelaka.user;

import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.auth.AuthenticationResponse;
import com.ali.antelaka.file.FileStorageService;
import com.ali.antelaka.user.dto.UserPublicProfileResponse;
import com.ali.antelaka.user.dto.UserSearchDTO;
import com.ali.antelaka.user.entity.User;
import com.ali.antelaka.user.request.ChangePasswordRequest;
import com.ali.antelaka.user.request.UpdateProfileRequest;
import com.ali.antelaka.user.service.OtpService;
import com.ali.antelaka.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
//@PreAuthorize("isAuthenticated()")
public class UserController {

    private final UserService service;

    @Autowired
    private UserRepository userRepository ;

    @Autowired
    private FileStorageService fileStorageService ;

    @Autowired
    private OtpService otpService;

    @Value("${server_file_url}")
    private String serverFileUrl;

    @PutMapping("/updateuserinfo")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse> updateBasicProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Principal connectedUser
    ) {

        User  user = user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        UserPublicProfileResponse updatedProfile = service.updateProfile(user, request);

        // 2. بناء استجابة ApiResponse
        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Basic profile information updated successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data(updatedProfile) // تمرير الـ DTO المحدث
                .build();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getUserProfile(
            @PathVariable Integer userId,
            Principal connectedUser
    ) {


        User user  = null ;
        if (connectedUser != null )user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();

        UserPublicProfileResponse profile  ;
        if (user != null && userId ==0   )
        {
            userId = user.getId() ;
        }
        if(user != null)
            profile = service.getUserProfileById(userId , user);
        else profile = service.getUserProfileById(userId , null);





        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("User profile retrieved successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data(profile)
                .build();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/hello")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public String hello (Authentication authentication)
    {
        String username = authentication.getName();

        var authorities = authentication.getAuthorities();

        var user = (User) authentication.getPrincipal();
        System.out.println(user);
        return "Hello "+ user.getUsername();
    }

    @PostMapping("/changeprofileimage")
    public ResponseEntity<?> changeprofileimage(
            @RequestParam(value = "file", required = false) List<MultipartFile> files,
            Principal connectedUser
    ) throws IOException {

        var user = userRepository.findByEmail(connectedUser.getName())
                .orElseThrow();

        Map<String, Object> m = new HashMap<>();

         if (files == null || files.isEmpty()) {

            if (user.getImagePath() != null) {
                fileStorageService.deleteIfLocalImage(user.getImagePath(), serverFileUrl);
            }

            user.setImagePath(null);
            m.put("imgPath", null);
        }

        else {
            MultipartFile file = files.get(0);

            if (!file.isEmpty()) {
                List<String> storedFiles = fileStorageService.saveFiles(List.of(file));

                if (user.getImagePath() != null) {
                    fileStorageService.deleteIfLocalImage(user.getImagePath() , serverFileUrl);
                }

                user.setImagePath(serverFileUrl +storedFiles.get(0));
                m.put("imgPath", serverFileUrl + storedFiles.get(0));
            }
        }

        userRepository.save(user);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("update successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .data(m)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PatchMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
          Principal connectedUser
    ) {
        return ResponseEntity.ok().body(service.changePassword( request, connectedUser)  );
    }



    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchUsers(
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<UserSearchDTO> result = service.searchUsers(username, page, size);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Users fetched successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }
}
