package com.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.ChangePasswordRequest;
import com.lms.dto.CustomMessage;
import com.lms.dto.UserUpdateRequest;
import com.lms.model.Role;
import com.lms.service.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final MyUserDetailsService myUserDetailsService;
    private final ObjectMapper objectMapper;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(myUserDetailsService.findAllUsers());
    }

    @GetMapping("/students")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllStudents() {
        return ResponseEntity.ok(myUserDetailsService.findUserByRole(Role.ROLE_STUDENT));
    }
    @GetMapping("/instructors")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllInstructors() {
        return ResponseEntity.ok(myUserDetailsService.findUserByRole(Role.ROLE_INSTRUCTOR));
    }

    @PatchMapping("/{userId}/toggle-status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> toggleUser(@PathVariable Long userId) {
        return ResponseEntity.ok(myUserDetailsService.toggleUser(userId));
    }

/*
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        myUserDetailsService.deleteUser(userId);
        return ResponseEntity.ok(new CustomMessage("User deleted successfully", "success"));
    }*/

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(myUserDetailsService.findUserById(userId));
    }


    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody UserUpdateRequest request, Principal principal) throws Exception {
        return ResponseEntity.ok(myUserDetailsService.updateUser(request,principal.getName()));
    }

    @PutMapping("/update-image")
    public ResponseEntity<?> updateUserImage(@RequestParam("file") MultipartFile file, Principal principal) throws Exception {
        myUserDetailsService.updateUserImage(file,principal.getName());
        return ResponseEntity.ok(new CustomMessage("Image updated successfully", "success"));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Principal principal) {
        return ResponseEntity.ok(myUserDetailsService.findUserByEmail(principal.getName()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest, Principal principal){
        myUserDetailsService.changePassword(principal.getName(), changePasswordRequest.getOldPassword(), changePasswordRequest.getNewPassword());
        return ResponseEntity.ok(new CustomMessage("Password changed successfully", "success"));
    }
}
