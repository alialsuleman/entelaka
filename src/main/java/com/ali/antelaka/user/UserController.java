package com.ali.antelaka.user;

import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.auth.AuthenticationResponse;
import com.ali.antelaka.file.FileStorageService;
import com.ali.antelaka.user.entity.User;
import com.ali.antelaka.user.request.ChangePasswordRequest;
import com.ali.antelaka.user.service.OtpService;
import com.ali.antelaka.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<?> changeprofileimage (
            @RequestParam("file") List<MultipartFile> files ,
            Principal connectedUser

    ) throws IOException {


        List<String> storedFiles = fileStorageService.saveFiles(files);

        var user = this.userRepository.findByEmail(connectedUser.getName()).orElseThrow() ;
        if (user.getImagePath() != null ){
            fileStorageService.deleteFile(user.getImagePath()) ;
        }

        Map m = new HashMap() ;
        if (!storedFiles.isEmpty()){
            user.setImagePath(storedFiles.get(0));
            m.put("imgPath" ,storedFiles.get(0) ) ;
        }
        else {
            user.setImagePath(null);
            m.put("imgPath" , null ) ;
        }


        this.userRepository.save(user) ;

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("update successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .data (m)
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
}
