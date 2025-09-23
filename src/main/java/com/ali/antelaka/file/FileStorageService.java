package com.ali.antelaka.file;



import com.ali.antelaka.post.repository.PostImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileStorageService {

    private final String uploadDir = "/home/ali/uploads"  ; // عدل حسب مكانك في الـ VPS

    @Autowired
    private PostImageRepository postImageRepository ;

    public List<String> saveFiles(List<MultipartFile> files) throws IOException {
        List<String> fileNames = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                // اسم فريد لكل صورة
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, fileName);

                Files.createDirectories(filePath.getParent());
                file.transferTo(filePath.toFile());

                fileNames.add(fileName);
            }
        }

        return fileNames;
    }

    public boolean deleteFile(Integer id ) {
        var img = this.postImageRepository.findById(id) ;
        String fileName = "" ;
        if (img.isPresent())
        {
            var x = img.get() ;
            fileName = x.getImageUrl();
            this.postImageRepository.delete(x);
        }
        Path filePath = Paths.get(uploadDir, fileName);
        File file = filePath.toFile();
        return file.exists() && file.delete();
    }
    public boolean deleteFile(String fileName ) {

        Path filePath = Paths.get(uploadDir, fileName);
        File file = filePath.toFile();
        return file.exists() && file.delete();
    }
}
