package com.ali.antelaka.ads;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdImageRepository extends JpaRepository<AdImage, Integer> {
}