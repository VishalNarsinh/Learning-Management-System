package com.lms.service;

import com.lms.dto.StoredFileInfo;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    StoredFileInfo saveFile(MultipartFile file, String directory);

    void deleteFile(String fileName);
}
