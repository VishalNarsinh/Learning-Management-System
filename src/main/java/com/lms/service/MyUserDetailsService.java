package com.lms.service;

import com.lms.dto.RegisterRequest;
import com.lms.dto.UserDto;
import com.lms.model.Role;

import java.util.List;

public interface MyUserDetailsService {

    UserDto saveUser(RegisterRequest registerRequest);

    void deleteUser(long userId);

    List<UserDto> findAllUsers();

    List<UserDto> findUserByRole(Role role);

    boolean toggleUser(Long userId);

    UserDto findUserById(Long userId);
}
