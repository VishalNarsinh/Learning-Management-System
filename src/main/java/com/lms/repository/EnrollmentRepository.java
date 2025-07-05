package com.lms.repository;

import com.lms.model.Course;
import com.lms.model.Enrollment;
import com.lms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment,Long> {

    Optional<Enrollment> findByUserAndCourse(User user, Course course);

    List<Enrollment> findByUser(User user);

    List<Enrollment> findByCourse_CourseId(long courseId);

}
