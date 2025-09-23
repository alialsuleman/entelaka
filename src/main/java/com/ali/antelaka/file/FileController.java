package com.ali.antelaka.file;

import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.post.entity.PostImage;
import com.ali.antelaka.post.repository.PostImageRepository;
import com.ali.antelaka.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private UserRepository  userRepository;

    //
    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadFiles(
            @RequestParam("files") List<MultipartFile> files
    ) throws IOException {
        List<String> storedFiles = fileStorageService.saveFiles(files);
        return ResponseEntity.ok(storedFiles);
    }

    @PostMapping("/uploadpostfile")
    public ResponseEntity<?> uploadPostFile(
            @RequestParam("files") List<MultipartFile> files
    ) throws IOException {
        List<String> storedFiles = fileStorageService.saveFiles(files);
        Map m = new HashMap<Integer , String>() ;
        storedFiles.forEach(x->{
            PostImage postImage = PostImage.builder()
                    .imageUrl(x)
                    .build();
            var savedImage = this.postImageRepository.save(postImage) ;
            m.put(savedImage.getId()  , savedImage.getImageUrl());
        });

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("created successfully")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .data (m)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }




    // حذف صورة
    @DeleteMapping("/deletepostimage/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable Integer id) {
        System.out.println(id);
        boolean deleted = fileStorageService.deleteFile(id);

        if (deleted) {
            return ResponseEntity.ok("Deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("File not found");
        }
    }
}
