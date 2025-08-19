package com.ali.antelaka.user;

import java.util.Optional;

import com.ali.antelaka.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findByEmail(String email);

}
