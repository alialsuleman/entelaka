package com.ali.antelaka.post.entity;


import com.ali.antelaka.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Builder.Default()
    private Integer numberOfLike =0 ;

    @Builder.Default()
    private Integer numberOfSubComment =0 ;



    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_parent_id")
    @JsonIgnore
    private Comment commentParent;

    @OneToMany(mappedBy = "commentParent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Comment> replies = new ArrayList<>();



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

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<LikeOnComment> likeOnComments ;

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
