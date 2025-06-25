package com.lms.service.impl;

import com.lms.dto.StoredFileInfo;
import com.lms.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);
//    @Value("${files.uploads.resource}")
//    private String resourceDir;
//
//    @Value("${files.uploads.pdf}")
//    private String pdfDir;
//
//    @Value("${files.uploads.image}")
//    private String imageDir;
//
//    @Value("${files.uploads.video}")
//    private String videoDir;


//    @PostConstruct
//    public void init() {
//        try {
//            createFolderIfNotExists(resourceDir);
//            createFolderIfNotExists(pdfDir);
//            createFolderIfNotExists(imageDir);
//            createFolderIfNotExists(videoDir);
//            log.info("All upload directories are initialized.");
//        } catch (IOException e) {
//            log.error("Error initializing upload directories: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to initialize upload folders", e);
//        }
//    }

    private void createFolderIfNotExists(String pathStr) throws IOException {
        Path path = Paths.get(pathStr).toAbsolutePath().normalize();
        Files.createDirectories(path); // Creates folder if it doesn't exist
    }

    @Override
    public StoredFileInfo saveFile(MultipartFile file, String directory) {
        try {
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String objectName = UUID.randomUUID() + "-" + originalFileName;

            Path dirPath = Paths.get(directory).toAbsolutePath().normalize();
            Files.createDirectories(dirPath); // just in case

            Path fullPath = dirPath.resolve(objectName);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, fullPath, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("✅ File saved: {}", fullPath);

            return StoredFileInfo.builder()
                    .originalFileName(originalFileName)
                    .objectName(objectName)
                    .fileUrl(fullPath.toString())
                    .contentType(file.getContentType())
                    .build();

        } catch (IOException e) {
            log.error("Error saving file: {}", e.getMessage(), e);
            throw new RuntimeException("Could not save file.", e);
        }
    }

    @Override
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("🗑 File deleted: {}", filePath);
            } else {
                log.warn("⚠ File not found for deletion: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Error deleting file: {}", e.getMessage(), e);
            throw new RuntimeException("Could not delete file.", e);
        }
    }
}
