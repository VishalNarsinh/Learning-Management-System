package com.lms.startup;

import com.lms.model.Image;
import com.lms.repository.ImageRepository;
import com.lms.utils.AppConstants;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

@Component
@RestController
@Order(1)
public class ImageInitializer implements CommandLineRunner {
    private final ImageRepository imageRepository;

    public ImageInitializer(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (imageRepository.count() > 0) {
            return;
        }
        Image image = Image.builder()
                .imageUrl(AppConstants.DEFAULT_USER_IMAGE)
                .fileName(AppConstants.DEFAULT_USER_IMAGE_NAME)
                .contentType("image/jpeg")
                .objectName(AppConstants.DEFAULT_USER_IMAGE_NAME)
                .build();
        imageRepository.save(image);
    }
}
