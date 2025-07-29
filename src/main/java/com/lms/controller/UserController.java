package com.lms.controller;

import com.lms.dto.CustomMessage;
import com.lms.model.Role;
import com.lms.service.MyUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final MyUserDetailsService myUserDetailsService;

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
    public ResponseEntity<?> toggleUser(@PathVariable Long userId) {
        return ResponseEntity.ok(myUserDetailsService.toggleUser(userId));
    }


    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        myUserDetailsService.deleteUser(userId);
        return ResponseEntity.ok(new CustomMessage("User deleted successfully", "success"));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(myUserDetailsService.findUserById(userId));
    }
}
