package com.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.CourseDto;
import com.lms.dto.CustomMessage;
import com.lms.exception.ApiException;
import com.lms.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private static final Logger log = LoggerFactory.getLogger(CourseController.class);
    private final CourseService courseService;
    private final ObjectMapper objectMapper;

//    @PostMapping(value = "/",consumes = {"multipart/form-data"})
//    public ResponseEntity<?> saveCourse(@RequestPart("course")CourseDto courseDto,@RequestPart("file") MultipartFile file) throws  IOException {
//        return ResponseEntity.ok(courseService.saveCourse(courseDto,file));
//    }


    @PostMapping(value = "/")
    public ResponseEntity<?> saveCourse(@RequestParam("course")String courseDtoData, @RequestParam("file") MultipartFile file, Principal principal) throws IOException {
        String email = principal.getName();
        CourseDto courseDto = objectMapper.readValue(courseDtoData, CourseDto.class);
        return ResponseEntity.ok(courseService.saveCourse(courseDto,file,email));
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<?> getCourseById(@PathVariable long courseId) {
        return ResponseEntity.ok(courseService.findCourseByCourseId(courseId));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllCourses() {
        return ResponseEntity.ok(courseService.findAll());
    }

    @GetMapping("/all-enabled")
    public ResponseEntity<?> getAllEnabledCourses() {
        return ResponseEntity.ok(courseService.findAllByEnabledTrue());
    }

    @PutMapping(value = "/{courseId}",consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateCourse(@PathVariable long courseId, @RequestParam("course")String courseDtoData,@RequestParam(value = "file",required = false) MultipartFile file,Principal principal) {
        try {
            CourseDto courseDto = objectMapper.readValue(courseDtoData, CourseDto.class);
            return ResponseEntity.ok(courseService.updateCourse(courseDto, courseId, file, principal.getName()));
        } catch (IOException e) {
            throw new ApiException("Error while processing course image"+e.getMessage());
        }
    }



    @DeleteMapping("/{courseId}")
    public ResponseEntity<?> deleteCourse(@PathVariable long courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.ok(CustomMessage.builder().message("Course deleted successfully").status("success").build());
    }

    @PatchMapping("/{courseId}/toggle-status")
    public ResponseEntity<?> toggleCourse(@PathVariable Long courseId,Principal principal) {
        return ResponseEntity.ok(courseService.toggleCourse(courseId,principal.getName()));
    }

    @GetMapping("/subcategory/{subCategoryId}")
    public ResponseEntity<?> getCourseBySubCategoryId(@PathVariable Long subCategoryId) {
        return ResponseEntity.ok(courseService.findCourseBySubCategoryId(subCategoryId));
    }

    @GetMapping("/popular")
    public ResponseEntity<?> getPopularCourses() {
        return ResponseEntity.ok(courseService.getPopularCoursesByEnrollment());
    }

    @GetMapping("/popular-with-rating")
    public ResponseEntity<?> getPopularCoursesByEnrollmentAndRating() {
        return ResponseEntity.ok(courseService.getPopularCoursesByEnrollmentAndRating());
    }

    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<?> getCoursesByInstructorId(@PathVariable Long instructorId) {
        return ResponseEntity.ok(courseService.findCourseByInstructorId(instructorId));
    }

    @GetMapping("/instructor/my-courses")
    public ResponseEntity<?> getMyCourses(Principal principal) {
        return ResponseEntity.ok(courseService.findCourseOfLoggedInInstructor(principal.getName()));
    }
}
