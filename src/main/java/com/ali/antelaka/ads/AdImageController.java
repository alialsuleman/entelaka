package com.ali.antelaka.ads;


import com.ali.antelaka.ApiResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ad-images")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")  // فقط الأدمن والمدير
public class AdImageController {

    @Autowired
    private AdImageService adImageService;

    // رفع صور إعلانات (حتى 5 صور)
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> uploadAdImages(@RequestParam("files") List<MultipartFile> files) {
        try {
            List<AdImage> savedImages = adImageService.uploadAdImages(files);
            // إرجاع بيانات الصور المحفوظة (id والمسار)
            Map<Integer, String> result = new HashMap<>();
            for (AdImage img : savedImages) {
                result.put(img.getId(), img.getImageUrl());
            }
            ApiResponse response = ApiResponse.builder()
                    .success(true)
                    .message("Ad images uploaded successfully")
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.CREATED.value())
                    .data(result)
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse response = ApiResponse.builder()
                    .success(false)
                    .message("Upload failed: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .data(null)
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    // جلب جميع صور الإعلانات (قائمة مع id والمسار)
    @GetMapping
    public ResponseEntity<ApiResponse> getAllAdImages() {
        List<AdImage> images = adImageService.getAllAdImages();
        Map<Integer, String> data = new HashMap<>();
        for (AdImage img : images) {
            data.put(img.getId(), img.getImageUrl());
        }
        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Ad images retrieved")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .data(data)
                .build();
        return ResponseEntity.ok(response);
    }

    // حذف صورة إعلان معينة بواسطة id
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteAdImage(@PathVariable Integer id) {
        boolean deleted = adImageService.deleteAdImage(id);
        ApiResponse response = ApiResponse.builder()
                .success(deleted)
                .message(deleted ? "Ad image deleted successfully" : "Ad image not found")
                .timestamp(LocalDateTime.now())
                .status(deleted ? HttpStatus.OK.value() : HttpStatus.NOT_FOUND.value())
                .data(null)
                .build();
        if (deleted) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}