package com.lms.repository;

import com.lms.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image,Long> {
    Optional<Image> findByFileName(String defaultUserImageName);
}
