package com.ali.antelaka.post.entity;


import com.ali.antelaka.page.entity.PageEntity;
import com.ali.antelaka.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private  String text ;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PostImage> postImages;
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LikeEntity> likes;
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> comments;


    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SaveEntity> saves;


    @Builder.Default()
    private Integer numberOfLikes  =0 ;

    @Builder.Default()
    private Integer numberOfComment  =0 ;


    @Builder.Default
    private Boolean isPublic = true;

    @Builder.Default()  private boolean isUpdated = false ;
    @Builder.Default()  private LocalDateTime createdAt = LocalDateTime.now() ;
    @Builder.Default()  private LocalDateTime updatedAt =  LocalDateTime.now() ;
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.isUpdated = true ;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id")
    @JsonIgnore
    private PageEntity pageEntity  ;

    private  String tag ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user  ;



}
