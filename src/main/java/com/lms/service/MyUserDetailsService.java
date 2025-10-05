package com.lms.service;

import com.lms.dto.RegisterRequest;
import com.lms.dto.UserDto;
import com.lms.model.Role;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MyUserDetailsService extends UserDetailsService {

    UserDto findUserByEmail(String email);

    UserDto saveUser(RegisterRequest registerRequest);

    UserDto updateUser(RegisterRequest registerRequest, MultipartFile file,String email) throws IOException;

    void deleteUser(long userId);

    void changePassword(String email, String oldPassword, String newPassword);

    List<UserDto> findAllUsers();

    List<UserDto> findUserByRole(Role role);

    boolean toggleUser(Long userId);

    UserDto findUserById(Long userId);
}
