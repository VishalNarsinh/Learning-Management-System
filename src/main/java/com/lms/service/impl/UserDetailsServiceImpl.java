package com.lms.service.impl;

import com.lms.dto.RegisterRequest;
import com.lms.dto.UserDto;
import com.lms.exception.ResourceNotFoundException;
import com.lms.mapper.UserMapper;
import com.lms.model.Role;
import com.lms.model.User;
import com.lms.repository.UserRepository;
import com.lms.service.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService, MyUserDetailsService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder bcryptPasswordEncoder;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(()->new UsernameNotFoundException("Invalid email or password"));
    }


    @Override
    public UserDto saveUser(RegisterRequest registerRequest){
        User user = UserMapper.toEntity(registerRequest);
        user.setRole(registerRequest.getRole());
        user.setEnabled(true);
        user.setPassword(bcryptPasswordEncoder.encode(user.getPassword()));
        User save = userRepository.save(user);
        return UserMapper.toDto(save);
    }

    @Override
    public void deleteUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        userRepository.delete(user);
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
