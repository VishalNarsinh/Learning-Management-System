package com.lms.service.impl;

import com.lms.dto.RegisterRequest;
import com.lms.dto.UserDto;
import com.lms.exception.IncorrectPasswordException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.exception.SamePasswordException;
import com.lms.mapper.UserMapper;
import com.lms.model.Image;
import com.lms.model.Role;
import com.lms.model.User;
import com.lms.repository.ImageRepository;
import com.lms.repository.UserRepository;
import com.lms.service.ImageService;
import com.lms.service.MyUserDetailsService;
import com.lms.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements  MyUserDetailsService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder bcryptPasswordEncoder;
    private final ImageRepository imageRepository;
    private final ImageService imageService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(()->new UsernameNotFoundException("Invalid email or password"));
    }

    @Override
    public UserDto findUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return UserMapper.toDto(user);
    }


    @Override
    public UserDto saveUser(RegisterRequest registerRequest){
        User user = UserMapper.toEntity(registerRequest);
        user.setRole(registerRequest.getRole());
        user.setEnabled(true);
        user.setPassword(bcryptPasswordEncoder.encode(user.getPassword()));
        Image image = imageRepository.findByFileName(AppConstants.DEFAULT_USER_IMAGE_NAME).get();
        user.setImage(image);
        User save = userRepository.save(user);
        return UserMapper.toDto(save);
    }

    @Override
    public UserDto updateUser(RegisterRequest registerRequest, MultipartFile file, String email) throws IOException {
        User oldUser = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        oldUser.setFirstName(registerRequest.getFirstName());
        oldUser.setLastName(registerRequest.getLastName());
        if(file != null && !file.isEmpty()) {
            Image image = oldUser.getImage();
            oldUser.setImage(null);
            if (image != null && !AppConstants.DEFAULT_USER_IMAGE_NAME.equals(image.getFileName())) {
                imageService.deleteImage(image.getImageId());
            }
            Image newImage = imageService.uploadImage(file, AppConstants.USER_IMAGE_FOLDER);
            oldUser.setImage(newImage);
        }
        User save = userRepository.save(oldUser);
        return UserMapper.toDto(save);
    }

    @Override
    public void deleteUser(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Image image = user.getImage();
        user.setImage(null);
        userRepository.delete(user);
        if (image != null && !AppConstants.DEFAULT_USER_IMAGE_NAME.equals(image.getFileName())) {
            imageService.deleteImage(image.getImageId());
        }
    }

    @Override
    public void changePassword(String email, String oldPassword, String newPassword){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        if(!bcryptPasswordEncoder.matches(oldPassword, user.getPassword())){
            throw new IncorrectPasswordException("You have entered incorrect old password");
        }
        if(oldPassword.equals(newPassword)){
            throw new SamePasswordException("New password cannot be same as old password");
        }
        user.setPassword(bcryptPasswordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public List<UserDto> findAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> findUserByRole(Role role) {
        return userRepository.findByRole(role).stream().map(UserMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public boolean toggleUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setEnabled(!user.isEnabled());
        User saved = userRepository.save(user);
        return saved.isEnabled();
    }

    @Override
    public UserDto findUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return UserMapper.toDto(user);
    }
}
