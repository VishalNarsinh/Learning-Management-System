package com.lms.service.impl;

import com.lms.dto.CourseDto;
import com.lms.dto.PopularCourseDto;
import com.lms.exception.ResourceNotFoundException;
import com.lms.mapper.CourseMapper;
import com.lms.model.*;
import com.lms.repository.CourseRepository;
import com.lms.repository.SubCategoryRepository;
import com.lms.repository.UserRepository;
import com.lms.repository.projection.PopularCourseProjection;
import com.lms.repository.projection.PopularCourseWithRatingProjection;
import com.lms.service.CourseService;
import com.lms.service.ImageService;
import com.lms.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private static final Logger log = LoggerFactory.getLogger(CourseServiceImpl.class);
//    private final SubCategoryService subCategoryService;
    private final CourseRepository courseRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ImageService imageService;
    private final UserRepository userRepository;
    private final CourseMapper courseMapper;




    @Override
    public CourseDto saveCourse(CourseDto courseDto, MultipartFile file,String email) throws IOException {
        SubCategory subCategory = subCategoryRepository.findById(courseDto.getSubCategoryId()).orElseThrow(() -> new ResourceNotFoundException("SubCategory", "id", courseDto.getSubCategoryId()));
        Course course = courseMapper.toEntity(courseDto);
        course.setSubCategory(subCategory);
        course.setEnabled(true);
        if(file!=null && !file.isEmpty()){
            Image image = imageService.uploadImage(file, AppConstants.COURSE_IMAGE_FOLDER);
            log.info("image {}", image);
            course.setImage(image);
        }
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        course.setInstructor(user);
        return courseMapper.toDto(courseRepository.save(course));
    }

    @Override
    @Transactional
    public void deleteCourse(long courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        log.info("deleting course {}", course);
        imageService.deleteImage(course.getImage().getImageId());
        course.setImage(null);
//        courseRepository.save(course);
//        courseRepository.delete(course);
        courseRepository.deleteCourseById(courseId);
    }

    @Transactional
    @Override
    public CourseDto updateCourse(CourseDto courseDto,long courseId,MultipartFile file,String instructorEmail) throws IOException {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        if(!course.getInstructor().getEmail().equals(instructorEmail)){
            throw new AccessDeniedException("You are not allowed to update this course");
        }
        if(file != null && !file.isEmpty()) {
            if(course.getImage() != null){
                Image image = course.getImage();
                course.setImage(null);
//                courseRepository.save(course);
                imageService.deleteImage(image.getImageId());
            }
            Image image = imageService.uploadImage(file, AppConstants.COURSE_IMAGE_FOLDER);
            course.setImage(image);
        }
        course.setCourseName(courseDto.getCourseName());
        course.setCourseDescription(courseDto.getCourseDescription());
        course.setSubCategory(subCategoryRepository.findById(courseDto.getSubCategoryId()).orElseThrow(() -> new ResourceNotFoundException("SubCategory", "id", courseDto.getSubCategoryId())));
        return courseMapper.toDto(courseRepository.save(course));
    }

    @Override
    public List<CourseDto> findCourseBySubCategoryId(Long subCategoryId) {
        List<Course> courseBySubCategory = courseRepository.findCourseBySubCategoryAndEnabledTrue(subCategoryRepository.findById(subCategoryId).orElseThrow(() -> new ResourceNotFoundException("SubCategory", "id", subCategoryId)));
        return courseBySubCategory.stream().map(courseMapper::toDto).toList();
    }

    @Override
    public CourseDto findCourseByCourseId(long courseId) {
//        return courseToDto(courseRepository.findById(courseId).orElseThrow(()->new ResourceNotFoundException("Course","id",courseId)));
        return courseMapper.toDto(courseRepository.findById(courseId).orElseThrow(()->new ResourceNotFoundException("Course","id",courseId)));
    }

    @Override
    public List<CourseDto> findAll() {
        return courseRepository.findAll().stream().map(courseMapper::toDto).toList();
    }

    @Override
    public List<CourseDto> findAllByEnabledTrue() {
        return courseRepository.findAllByEnabledTrue().stream().map(courseMapper::toDto).toList();
    }


    @Override
    public List<CourseDto> findCourseByInstructorId(Long instructorId) {
        return courseRepository.findByInstructor_UserIdAndEnabledTrue(instructorId).stream().map(courseMapper::toDto).toList();
    }

    @Override
    public List<CourseDto> findCourseOfLoggedInInstructor(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return courseRepository.findByInstructor_UserId(user.getUserId()).stream().map(courseMapper::toDto).toList();
    }

    @Override
    public boolean toggleCourse(Long courseId,String email) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new ResourceNotFoundException("Course", "id", courseId));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        if (user.getRole().equals(Role.ROLE_ADMIN) ||course.getInstructor().getEmail().equals(email)) {
            course.setEnabled(!course.isEnabled());
        }
        else {
            throw new AccessDeniedException("You are not authorized to toggle this course.");
        }
        Course saved = courseRepository.save(course);
        return saved.isEnabled();
    }

    @Override
    public List<PopularCourseDto> getPopularCoursesByEnrollment() {
        List<PopularCourseProjection> popularCourses = courseRepository.findPopularCourses(PageRequest.of(0, 5));
        return popularCourses.stream().map(p -> {
            PopularCourseDto dto = new PopularCourseDto();
            dto.setCourse(courseMapper.toDto(p.getCourse()));
            dto.setEnrollmentCount(p.getEnrollmentCount());
            return dto;
        }).toList();
    }

    @Override
    public List<PopularCourseDto> getPopularCoursesByEnrollmentAndRating() {
        List<PopularCourseWithRatingProjection> popularCoursesWithRatings = courseRepository.findPopularCoursesWithRatings(PageRequest.of(0, 5));
        return popularCoursesWithRatings.stream().map(c->{
            PopularCourseDto dto = new PopularCourseDto();
            dto.setCourse(courseMapper.toDto(c.getCourse()));
            dto.setEnrollmentCount(c.getEnrollmentCount());
            dto.setAverageRating(c.getAverageRating());
            return dto;
        }).toList();
    }



}
