package com.lms.repository;

import com.lms.model.Enrollment;
import com.lms.model.Lesson;
import com.lms.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByEnrollmentAndLesson(Enrollment enrollment, Lesson lesson);


    long countByEnrollment_EnrollmentIdAndCompletedTrue(Long enrollmentId);

    void deleteLessonProgressesByLesson_LessonId(Long lessonId);
}
