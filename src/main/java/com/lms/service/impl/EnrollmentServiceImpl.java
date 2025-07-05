package com.lms.service.impl;

import com.lms.dto.EnrollmentResponse;
import com.lms.exception.ResourceNotFoundException;
import com.lms.mapper.EnrollmentMapper;
import com.lms.model.Course;
import com.lms.model.Enrollment;
import com.lms.model.User;
import com.lms.repository.*;
import com.lms.service.EnrollmentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository, UserRepository userRepository, CourseRepository courseRepository, LessonRepository lessonRepository, LessonProgressRepository lessonProgressRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.lessonProgressRepository = lessonProgressRepository;
    }

    @Override
    public Enrollment enrollUserByEmail(String email, Long courseId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));

        Optional<Enrollment> existing = enrollmentRepository.findByUserAndCourse(user, course);
        if (existing.isPresent()) {
            return existing.get();
        }

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .completed(false)
                .build();

        return enrollmentRepository.save(enrollment);
    }

    @Override
    public List<EnrollmentResponse> getMyEnrollment(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        List<Enrollment> enrollments = enrollmentRepository.findByUser(user);
        return enrollments.stream().map(EnrollmentMapper::toResponse).toList();
    }

    @Override
    public Double getCourseProgressPercentage(String email, Long enrollmentId) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(() -> new ResourceNotFoundException("Enrollment", "enrollmentId", enrollmentId));
        long courseId = enrollment.getCourse().getCourseId();
        long totalLesson = lessonRepository.countByCourse_CourseId(courseId);
        if (totalLesson == 0) {
            return 0.0;
        }
        long completedLesson = lessonProgressRepository.countByEnrollment_EnrollmentIdAndCompletedTrue(enrollmentId);
        double percentage = ((double) completedLesson / totalLesson) * 100.0;
        return Math.round(percentage * 100.0) / 100.0;
    }

    @Override
    public List<EnrollmentResponse> getEnrollmentsByCourse(Long courseId) {
        List<Enrollment> byCourseCourseId = enrollmentRepository.findByCourse_CourseId(courseId);
        return byCourseCourseId.stream().map(EnrollmentMapper::toResponse).toList();
    }


    @Override
    public List<EnrollmentResponse> getEnrollmentsByUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        List<Enrollment> enrollments = enrollmentRepository.findByUser(user);
        return enrollments.stream().map(EnrollmentMapper::toResponse).toList();
    }

    @Override
    public boolean markEnrollmentAsCompleted(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "enrollmentId", enrollmentId));
        if (enrollment.isCompleted()) return true;
        long courseId = enrollment.getCourse().getCourseId();
        long totalLesson = lessonRepository.countByCourse_CourseId(courseId);
        long completedLesson = lessonProgressRepository.countByEnrollment_EnrollmentIdAndCompletedTrue(enrollmentId);
        if (completedLesson != totalLesson) {
            return false;
        }
        enrollment.setCompleted(true);
        enrollmentRepository.save(enrollment);
        return true;
    }

}
