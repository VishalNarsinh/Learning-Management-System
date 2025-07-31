package com.lms.service.impl;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.lms.model.Image;
import com.lms.service.GCSService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Profile("!ci")
@Primary
@RequiredArgsConstructor
public class GCSServiceImpl implements GCSService {
    private static final Logger log = LoggerFactory.getLogger(GCSServiceImpl.class);
    private final Storage storage;

    @Value("${gcs.bucket.name}")
    private String bucketName;
    @Override
    public Image uploadFile(MultipartFile file, String folder) throws IOException {


//        storage.
        String objectName = folder+ "/"+ UUID.randomUUID().toString()+ "-" + file.getOriginalFilename();

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());
        storage.createAcl(blobInfo.getBlobId(), Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

        String imageUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, objectName);

        Image image = Image.builder()
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .objectName(objectName)
                .imageUrl(imageUrl)
                .build();
        return image;
    }

    @Override
    public boolean deleteFile(String objectName) {
        log.info("Deleting file {}", objectName);
        return storage.delete(bucketName, objectName);
    }
}
