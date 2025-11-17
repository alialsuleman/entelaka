package com.ali.antelaka.user.entity;

import com.ali.antelaka.follow.Follow;
import com.ali.antelaka.page.entity.PageEntity;
import com.ali.antelaka.post.entity.LikeOnComment;
import com.ali.antelaka.post.entity.Post;
import com.ali.antelaka.post.entity.SaveEntity;
import com.ali.antelaka.token.Token;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_user")
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String firstname;
  private String lastname;

  @Column(name = "image_path")
  private String imagePath;




  @Email(message = "Email must be valid")
  @NotBlank(message = "Email required")
  @Column(unique = true)
  private String email;
  private String password;



  @Enumerated(EnumType.STRING)
  private Role role;


  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<PageEntity> pages;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<Post> posts;

  @OneToMany(mappedBy = "user" ,cascade = CascadeType.ALL ,  orphanRemoval = true, fetch = FetchType.LAZY)
  private List<Token> tokens;


  @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true ,  fetch = FetchType.LAZY)
  private List<Follow> following;

  @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true ,  fetch = FetchType.LAZY)
  private List<Follow> followers;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LikeOnComment> likeOnComments;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JsonIgnore
  private List<SaveEntity> saves;

  private boolean enabled  ;


  private String otp;
  private String resetPasswordOtp;


  @Builder.Default()  private Integer resetPasswordOtpAttempts =0  ;
  @Builder.Default()  private Integer numberOfresetPasswordOtpSending =0  ;

  @Builder.Default()  private LocalDateTime otpExpirationTime= LocalDateTime.now().minusMinutes(10)   ;

  @Builder.Default()  private LocalDateTime maxTimeToResetPassword= LocalDateTime.now().minusMinutes(10)   ;

  @Builder.Default()  private LocalDateTime resetPasswordOtpExpirationTime = LocalDateTime.now().minusMinutes(10)   ;
  @Builder.Default()  private LocalDateTime resetPasswordOTPSendingBanTime = LocalDateTime.now().minusMinutes(10)   ;


  @Builder.Default()  private Integer numberOfOtpSending  =0 ;
  @Builder.Default()  private Integer attempts =0  ;
  @Builder.Default()  private LocalDateTime lastOtpSentAt = LocalDateTime.now().minusMinutes(10)    ;
  @Builder.Default()  private LocalDateTime lastResetPasswordOTPSentAt = LocalDateTime.now().minusMinutes(10)  ;
  @Builder.Default()  private LocalDateTime OTPSendingBanTime = LocalDateTime.now().minusMinutes(10)    ;



  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return role.getAuthorities();
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return this.enabled;
  }

  public void setEnabled(boolean  x ) {
     this.enabled =x ;
  }
}
