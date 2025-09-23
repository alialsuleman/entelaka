package com.ali.antelaka.page.entity;


import com.ali.antelaka.post.entity.Post;
import com.ali.antelaka.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

//
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class PageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String tag ;
    private String pageType ;
    private String description ;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user  ;

    @OneToMany(mappedBy = "pageEntity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Post> posts;



}
