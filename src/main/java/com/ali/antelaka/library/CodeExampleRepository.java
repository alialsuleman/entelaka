package com.ali.antelaka.library;

import com.ali.antelaka.library.CodeExample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CodeExampleRepository extends JpaRepository<CodeExample, Long> {

    List<CodeExample> findByLanguageIgnoreCase(String language);

    List<CodeExample> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT c FROM CodeExample c WHERE LOWER(c.language) = LOWER(:language) AND LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<CodeExample> searchByLanguageAndTitle(@Param("language") String language, @Param("search") String search);

    long countByLanguageIgnoreCase(String language);

    void deleteByLanguageIgnoreCase(String language);
}