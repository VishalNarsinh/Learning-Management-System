package com.lms.dto;

import com.lms.model.Image;
import com.lms.model.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private long userId;
    private String firstName;
    private String lastName;
    private String email;
    private boolean enabled;
    private Role role;
    private Image image;
}