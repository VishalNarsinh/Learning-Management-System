package com.lms.repository;

import com.lms.model.Course;
import com.lms.model.SubCategory;
import com.lms.repository.projection.PopularCourseProjection;
import com.lms.repository.projection.PopularCourseWithRatingProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course,Long> {
    @Modifying
    @Query("delete from Course c where c.courseId = :courseId")
    void deleteCourseById(long courseId);

    List<Course> findCourseBySubCategoryAndEnabledTrue(SubCategory subCategory);


    List<Course> findByInstructor_UserId(long instructorUserId);


    List<Course> findAllByEnabledTrue();

    @Query("""
        SELECT c as course, COUNT(e) as enrollmentCount
        FROM Course c
        LEFT JOIN Enrollment e ON e.course = c
        WHERE c.enabled = true
        GROUP BY c
        ORDER BY COUNT(e) DESC
    """)
    List<PopularCourseProjection> findPopularCourses(Pageable pageable);

    @Query("""
    SELECT c AS course, COUNT(DISTINCT e) AS enrollmentCount, COALESCE(AVG(DISTINCT r.rating), 0.0) AS averageRating
    FROM Course c
    LEFT JOIN Enrollment e ON e.course = c
    LEFT JOIN CourseRating r ON r.course = c
    WHERE c.enabled = true
    GROUP BY c
    ORDER BY COUNT(e) DESC, AVG(r.rating) DESC
""")
    List<PopularCourseWithRatingProjection> findPopularCoursesWithRatings(Pageable pageable);


}
