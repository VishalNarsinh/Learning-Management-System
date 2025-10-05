package com.lms.service.impl;

import com.lms.exception.ResourceNotFoundException;
import com.lms.model.Image;
import com.lms.repository.ImageRepository;
import com.lms.service.GCSService;
import com.lms.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    private static final Logger log = LoggerFactory.getLogger(ImageServiceImpl.class);
    private final GCSService gcsService;
    private final ImageRepository imageRepository;

    @Override
    public Image
    uploadImage(MultipartFile file, String folder) throws IOException {
        if(file == null || file.isEmpty()) {
            throw new BadRequestException("File is null or empty");
        }
        Image image = gcsService.uploadFile(file, folder);
        Image save = imageRepository.saveAndFlush(image);
        log.info("saved : image {}", save);
        return save;
    }

    @Override
    public void deleteImage(Long imageId) {
        imageRepository.findById(imageId).ifPresentOrElse(image -> {
            gcsService.deleteFile(image.getObjectName());
            imageRepository.delete(image);
        } , () ->{
            throw new ResourceNotFoundException("Image", "id", imageId);
        });
    }
}
