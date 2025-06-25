package com.lms.repository;

import com.lms.model.Course;
import com.lms.model.SubCategory;
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

}
