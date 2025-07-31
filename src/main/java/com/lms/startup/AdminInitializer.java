package com.lms.startup;

import com.lms.model.Image;
import com.lms.model.Role;
import com.lms.model.User;
import com.lms.repository.ImageRepository;
import com.lms.repository.UserRepository;
import com.lms.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(2)
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageRepository imageRepository;

    @Override
    public void run(String... args) throws Exception {
        if(userRepository.countByRole(Role.ROLE_ADMIN)==0){
            Image image = imageRepository.findByFileName(AppConstants.DEFAULT_USER_IMAGE_NAME).get();
            User admin = User.builder()
                    .firstName("Admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin"))
                    .role(Role.ROLE_ADMIN)
                    .image(image)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
        }
    }
}
