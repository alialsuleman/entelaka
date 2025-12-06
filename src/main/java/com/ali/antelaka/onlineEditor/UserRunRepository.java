package com.ali.antelaka.onlineEditor;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UserRunRepository extends JpaRepository<UserRun, Long> {

    Optional<UserRun> findByUserIdAndDate(Integer userId, LocalDate date);

    void deleteByUserIdAndDateBefore(Integer userId, LocalDate date);

}