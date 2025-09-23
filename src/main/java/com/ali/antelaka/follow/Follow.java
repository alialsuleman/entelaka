package com.ali.antelaka.follow;


import com.ali.antelaka.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_follow")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "follower_id")
    @JsonIgnore
    private User follower;

    @ManyToOne
    @JoinColumn(name = "following_id")
    @JsonIgnore
    private User following;

    @Builder.Default()
    private LocalDateTime createdAt = LocalDateTime.now();

}