package com.ali.antelaka.post.entity;


import com.ali.antelaka.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class Comment  {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String text ;

    @Builder.Default()  private LocalDateTime createdAt = LocalDateTime.now() ;
    @Builder.Default()  private LocalDateTime updatedAt = null  ;
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    @JsonIgnore
    private Post post  ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    public String getUserName() {
        return user != null ? user.getUsername() : null;
    }

    public Integer getUserId() {
        return user != null ? user.getId() : null;
    }
    public String getUserAvatar()
    {
        return user != null ? user.getImagePath() : null;
    }
}
