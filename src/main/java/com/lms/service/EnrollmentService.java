package com.lms.service;

import com.lms.dto.EnrollmentResponse;
import com.lms.model.Enrollment;

import java.util.List;

public interface EnrollmentService {
//    Enrollment enrollUserInCourse(Long userId, Long courseId);

    List<EnrollmentResponse> getEnrollmentsByUser(Long userId);

    boolean markEnrollmentAsCompleted(Long enrollmentId);

    Enrollment enrollUserByEmail(String email, Long courseId);

    List<EnrollmentResponse> getMyEnrollment(String email);

    Double getCourseProgressPercentage(String email,Long enrollmentId);

    List<EnrollmentResponse> getEnrollmentsByCourse(Long courseId);

}
