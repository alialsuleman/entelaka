package com.ali.antelaka.library;


import com.ali.antelaka.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/code-examples")
@RequiredArgsConstructor
public class LibraryController {

    @Autowired
    private LibraryService libraryService ;

    @GetMapping("/{language}")
    public ResponseEntity<ApiResponse<?>> getCppExamples(
            @PathVariable String language
    ) {

        ApiResponse<?> response = ApiResponse.builder()
                .success(true)
                .message("C++ code examples fetched successfully")
                .status(200)
                .timestamp(LocalDateTime.now())
                .data(libraryService.getCodeExamples(language))
                .build();

        return ResponseEntity.ok(response);
    }
}
