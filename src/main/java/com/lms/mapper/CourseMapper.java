package com.lms.mapper;

import com.lms.dto.CourseDto;
import com.lms.model.Course;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    private static final Logger log = LoggerFactory.getLogger(CourseMapper.class);
    private final LessonMapper lessonMapper;

    public CourseMapper(LessonMapper lessonMapper) {
        this.lessonMapper = lessonMapper;
    }

    public Course toEntity(CourseDto dto) {
//        log.info("Converting CourseDto to Course");
        if (dto == null) return null;

        Course course = new Course();
//        course.setCourseId(dto.getCourseId());
        course.setCourseName(dto.getCourseName());
        course.setCourseDescription(dto.getCourseDescription());
        course.setImage(dto.getImage()); // No mapper needed
        // subCategoryId will be set externally in service layer

//        if (dto.getLessons() != null) {
//            List<Lesson> lessons = dto.getLessons().stream()
//                    .map(lessonDto -> {
//                        Lesson lesson = lessonMapper.toEntity(lessonDto);
//                        lesson.setCourse(course); // back reference
//                        return lesson;
//                    })
//                    .collect(Collectors.toList());
//            course.setLessons(lessons);
//        }

        return course;
    }

    public CourseDto toDto(Course course) {
//        log.info("Converting Course to CourseDto");
        if (course == null) return null;

        CourseDto dto = new CourseDto();
        dto.setCourseId(course.getCourseId());
        dto.setCourseName(course.getCourseName());
        dto.setEnabled(course.isEnabled());
        dto.setCourseDescription(course.getCourseDescription());
        dto.setImage(course.getImage()); // No mapping needed
        dto.setSubCategoryId(course.getSubCategory() != null ? course.getSubCategory().getSubCategoryId() : null);

//        if (course.getLessons() != null) {
//            List<LessonDto> lessonDtos = course.getLessons().stream()
//                    .map(lessonMapper::toDto)
//                    .collect(Collectors.toList());
//            dto.setLessons(lessonDtos);
//        }

        return dto;
    }
}
