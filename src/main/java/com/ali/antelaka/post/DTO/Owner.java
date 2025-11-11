package com.ali.antelaka.post.DTO;


import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Owner {

   private Integer  userId ;
   private String username ;
   private String  userImagePath ;
   private boolean isMe  ;
   private boolean isIfollowingHim  ;


}
