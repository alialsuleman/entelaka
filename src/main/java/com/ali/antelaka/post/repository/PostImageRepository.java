package com.ali.antelaka.post.repository;


import com.ali.antelaka.post.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostImageRepository  extends JpaRepository<PostImage, Integer> {

}