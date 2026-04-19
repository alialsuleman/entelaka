package com.ali.antelaka.ads;


import com.ali.antelaka.file.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdImageService {

    private static final int MAX_IMAGES_PER_UPLOAD = 5;
    private static final String ADS_SUBDIR = "ads";

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AdImageRepository adImageRepository;

    // رفع قائمة من الملفات (بحد أقصى 5)
    public List<AdImage> uploadAdImages(List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) {
            throw new RuntimeException("No files provided");
        }
        if (files.size() > MAX_IMAGES_PER_UPLOAD) {
            throw new RuntimeException("Cannot upload more than " + MAX_IMAGES_PER_UPLOAD + " images at once");
        }

        // حفظ الملفات على القرص
        List<String> relativePaths = fileStorageService.saveFiles(files, ADS_SUBDIR);

        // حفظ مسارات الصور في قاعدة البيانات
        List<AdImage> savedImages = new ArrayList<>();
        for (String path : relativePaths) {
            AdImage image = AdImage.builder()
                    .imageUrl(path)
                    .build();
            savedImages.add(adImageRepository.save(image));
        }
        return savedImages;
    }

    // جلب جميع صور الإعلانات
    public List<AdImage> getAllAdImages() {
        return adImageRepository.findAll();
    }

    // حذف صورة إعلان حسب id (مع حذف الملف الفعلي)
    public boolean deleteAdImage(Integer id) {
        return adImageRepository.findById(id).map(image -> {
            // حذف الملف من القرص
            boolean fileDeleted = fileStorageService.deleteFileByRelativePath(image.getImageUrl());
            // حذف السجل من قاعدة البيانات
            adImageRepository.delete(image);
            return fileDeleted; // يمكن تجاهل نتيجة حذف الملف أو تسجيلها
        }).orElse(false);
    }
}