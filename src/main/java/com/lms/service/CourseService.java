package com.lms.service;

import com.lms.dto.CourseDto;
import com.lms.dto.PopularCourseDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CourseService {


    CourseDto saveCourse(CourseDto courseDto,MultipartFile file,String email) throws IOException;

    void deleteCourse(long courseId);

    CourseDto updateCourse(CourseDto courseDto,long courseId, MultipartFile file,String instructorEmail) throws IOException;

    List<CourseDto> findCourseBySubCategoryId(Long subCategoryId);

    CourseDto findCourseByCourseId(long courseId);

    List<CourseDto> findAll();

    List<CourseDto> findAllByEnabledTrue();

    List<CourseDto> findCourseByInstructorId(Long instructorId);

    boolean toggleCourse(Long courseId,String email);

    List<PopularCourseDto> getPopularCoursesByEnrollment();

    List<PopularCourseDto> getPopularCoursesByEnrollmentAndRating();


}
