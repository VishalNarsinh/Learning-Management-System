package com.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.ChangePasswordRequest;
import com.lms.dto.CustomMessage;
import com.lms.dto.RegisterRequest;
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
    public ResponseEntity<?> updateUser(@RequestParam("userData") String request, @RequestParam("file") MultipartFile file, Principal principal) throws Exception {
        RegisterRequest registerRequest = objectMapper.readValue(request, RegisterRequest.class);
        return ResponseEntity.ok(myUserDetailsService.updateUser(registerRequest,file,principal.getName()));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Principal principal) {
        return ResponseEntity.ok(myUserDetailsService.findUserByEmail(principal.getName()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest, Principal principal){
        myUserDetailsService.changePassword(changePasswordRequest.getOldPassword(), changePasswordRequest.getNewPassword(), principal.getName());
        return ResponseEntity.ok(new CustomMessage("Password changed successfully", "success"));
    }
}
