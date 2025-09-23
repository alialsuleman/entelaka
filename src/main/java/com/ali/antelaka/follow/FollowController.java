package com.ali.antelaka.follow;


import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.user.UserRepository;
import com.ali.antelaka.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final UserRepository userRepository ;



    @GetMapping("/{userId}/followers")
    public  ResponseEntity<ApiResponse<Page<User>>> getFollowers(
            @PathVariable Integer userId ,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            ) {
        var user = this.userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));;

        var x = followService.getFollowers(user , page, size) ;
        var data = x.map(xx ->{
            Map m =  new HashMap( );
            m.put("username" , xx.getUsername()) ;
            m.put("id" , xx.getId()) ;
            m.put("image" , xx.getImagePath()) ;
            return m ;
        }) ;
        ApiResponse res =  ApiResponse.builder()
                .success(true)
                .message("followers List :")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data (data )
                .build();

        return ResponseEntity.ok(res) ;


    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<ApiResponse<Page<User>>> getFollowing(
            @PathVariable Integer userId ,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size ) {
        var user = this.userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));;
        var x = followService.getFollowing(user , page, size) ;
        var data = x.map(xx ->{
            Map m =  new HashMap( );
            m.put("username" , xx.getUsername()) ;
            m.put("id" , xx.getId()) ;
            m.put("image" , xx.getImagePath()) ;
            return m ;
        }) ;
        ApiResponse res =  ApiResponse.builder()
                .success(true)
                .message("following List :")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data (data)
                .build();


        return ResponseEntity.ok(res) ;
    }


    @PostMapping("/follow/{followingId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity follow( @PathVariable Integer followingId
            , Principal connectedUser ) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        followService.followUser(user, followingId);
        ApiResponse res =  ApiResponse.builder()
                .success(true)
                .message("Done !")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .data (null)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(res) ;

    }

    @DeleteMapping("/unfollow/{followingId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity unfollow( @PathVariable Integer followingId
            , Principal connectedUser) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
        followService.unfollowUser(user, followingId);
        ApiResponse res =  ApiResponse.builder()
                .success(true)
                .message("Done !")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .data (null)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(res) ;
    }
}
