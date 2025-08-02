package com.lms.repository;

import com.lms.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson,Long> {
    @Modifying
    @Query("delete from Lesson l where l.lessonId = :lessonId")
    void deleteByLessonId(long lessonId);

    List<Lesson> findByCourseCourseId(Long courseId);
    List<Lesson> findByCourseCourseIdOrderBySequenceNumber(Long courseId);

    long countByCourse_CourseId(long courseCourseId);

    @Query("SELECT COALESCE(MAX(l.sequenceNumber), 0) FROM Lesson l WHERE l.course.courseId = :courseId")
    int getMaxSequenceNumberByCourseId(@Param("courseId") Long courseId);

    List<Lesson> findByCourse_CourseIdAndSequenceNumberBetweenOrderBySequenceNumberAsc(Long courseId, int start, int end);

}
