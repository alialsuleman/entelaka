package com.ali.antelaka.user.entity;

import com.ali.antelaka.token.Token;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "tokens")
@Entity
@Table(name = "_user")
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private String firstname;
  private String lastname;

  @Email(message = "Email must be valid")
  @NotBlank(message = "Email required")
  @Column(unique = true)
  private String email;
  private String password;



  @Enumerated(EnumType.STRING)
  private Role role;

  @OneToMany(mappedBy = "user")
  @JsonIgnore
  private List<Token> tokens;





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
