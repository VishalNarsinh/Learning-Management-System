package com.lms.controller;

import com.lms.dto.CustomMessage;
import com.lms.dto.EnrollmentRequest;
import com.lms.dto.EnrollmentResponse;
import com.lms.mapper.EnrollmentMapper;
import com.lms.model.Enrollment;
import com.lms.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<?> enrollUser(@RequestBody EnrollmentRequest request, Principal principal) {
        String email = principal.getName(); // From JWT or auth session

        Enrollment enrollment = enrollmentService.enrollUserByEmail(email, request.getCourseId());
        EnrollmentResponse enrollmentResponse = EnrollmentMapper.toResponse(enrollment);

        return ResponseEntity.ok(enrollmentResponse);
    }


    @PostMapping("/{enrollmentId}/complete")
    public ResponseEntity<?> markCourseAsCompleted(@PathVariable Long enrollmentId) {
        boolean completed = enrollmentService.markEnrollmentAsCompleted(enrollmentId);
        if (completed) {
            return ResponseEntity.ok(new CustomMessage("Course completed successfully", "success"));
        } else {
            return ResponseEntity.badRequest().body(new CustomMessage("Course is not yet fully completed", "error"));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserEnrollments(@PathVariable Long userId) {
        List<EnrollmentResponse> enrollments = enrollmentService.getEnrollmentsByUser(userId);
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/my-enrollment")
    public ResponseEntity<?> getMyEnrollment(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(enrollmentService.getMyEnrollment(email));
    }

    @GetMapping("/progress/{enrollmentId}")
    public ResponseEntity<?> getCourseProgressPercentage(@PathVariable Long enrollmentId, Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(enrollmentService.getCourseProgressPercentage(email,enrollmentId));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<?> getUsersByCourse(@PathVariable Long courseId){
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourse(courseId));
    }
}
