package com.ali.antelaka.library;

import com.ali.antelaka.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/code-examples")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    // GET - جلب كل الأمثلة
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllExamples() {
        ApiResponse<?> response = ApiResponse.builder()
                .success(true)
                .message("All code examples fetched successfully")
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .data(libraryService.getAllCodeExamples())
                .build();
        return ResponseEntity.ok(response);
    }

    // GET - جلب أمثلة حسب اللغة (محافظ على الشكل الأصلي)
    @GetMapping("/{language}")
    public ResponseEntity<ApiResponse<?>> getExamplesByLanguage(@PathVariable String language) {
        ApiResponse<?> response = ApiResponse.builder()
                .success(true)
                .message(language + " code examples fetched successfully")
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .data(libraryService.getCodeExamples(language))
                .build();
        return ResponseEntity.ok(response);
    }

    // GET - جلب مثال محدد حسب ID
    @GetMapping("/id/{id}")
    public ResponseEntity<ApiResponse<?>> getExampleById(@PathVariable Long id) {
        try {
            CodeExampleDTO example = libraryService.getCodeExampleById(id);
            ApiResponse<?> response = ApiResponse.builder()
                    .success(true)
                    .message("Code example fetched successfully")
                    .status(HttpStatus.OK.value())
                    .timestamp(LocalDateTime.now())
                    .data(example)
                    .build();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
                return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // GET - بحث في الأمثلة
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> searchExamples(
            @RequestParam String language,
            @RequestParam(required = false) String term) {
        ApiResponse<?> response = ApiResponse.builder()
                .success(true)
                .message("Search completed successfully")
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .data(libraryService.searchExamples(language, term))
                .build();
        return ResponseEntity.ok(response);
    }

    // GET - إحصائيات حسب اللغة
    @GetMapping("/stats/{language}")
    public ResponseEntity<ApiResponse<?>> getStats(@PathVariable String language) {
        long count = libraryService.getCountByLanguage(language);
        ApiResponse<?> response = ApiResponse.builder()
                .success(true)
                .message("Statistics for " + language)
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .data(Map.of("language", language, "count", count))
                .build();
        return ResponseEntity.ok(response);
    }

    // POST - إضافة مثال جديد
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<?>> createExample(@RequestBody CodeExampleDTO dto) {
        try {
            CodeExampleDTO created = libraryService.createCodeExample(dto);
            ApiResponse<?> response = ApiResponse.builder()
                    .success(true)
                    .message("Code example created successfully")
                    .status(HttpStatus.CREATED.value())
                    .timestamp(LocalDateTime.now())
                    .data(created)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return buildErrorResponse("Failed to create: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // PUT - تحديث كامل
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<?>> updateExample(
            @PathVariable Long id,
            @RequestBody CodeExampleDTO dto) {
        try {
            CodeExampleDTO updated = libraryService.updateCodeExample(id, dto);
            ApiResponse<?> response = ApiResponse.builder()
                    .success(true)
                    .message("Code example updated successfully")
                    .status(HttpStatus.OK.value())
                    .timestamp(LocalDateTime.now())
                    .data(updated)
                    .build();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // PATCH - تحديث جزئي
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<?>> patchExample(
            @PathVariable Long id,
            @RequestBody CodeExampleDTO dto) {
        try {
            CodeExampleDTO updated = libraryService.patchCodeExample(id, dto);
            ApiResponse<?> response = ApiResponse.builder()
                    .success(true)
                    .message("Code example partially updated successfully")
                    .status(HttpStatus.OK.value())
                    .timestamp(LocalDateTime.now())
                    .data(updated)
                    .build();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // DELETE - حذف مثال حسب ID
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<?>> deleteExample(@PathVariable Long id) {
        try {
            libraryService.deleteCodeExample(id);
            ApiResponse<?> response = ApiResponse.builder()
                    .success(true)
                    .message("Code example deleted successfully")
                    .status(HttpStatus.OK.value())
                    .timestamp(LocalDateTime.now())
                    .data(null)
                    .build();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // DELETE - حذف كل الأمثلة للغة معينة
    @DeleteMapping("/language/{language}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<?>> deleteByLanguage(@PathVariable String language) {
        libraryService.deleteByLanguage(language);
        ApiResponse<?> response = ApiResponse.builder()
                .success(true)
                .message("All " + language + " examples deleted successfully")
                .status(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .data(null)
                .build();
        return ResponseEntity.ok(response);
    }


    private ResponseEntity<ApiResponse<?>> buildErrorResponse(String message, HttpStatus status) {
        ApiResponse<?> response = ApiResponse.builder()
                .success(false)
                .message(message)
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .data(null)
                .build();
        return ResponseEntity.status(status).body(response);
    }


}