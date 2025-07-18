package com.lms.dto;

import com.lms.model.Image;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseDto {
    private long courseId;
    private String courseName;
    private String courseDescription;
    private boolean enabled;
    private Image image;
    private Long subCategoryId;
    private Long instructorId;
//    private List<LessonDto> lessons = new ArrayList<>();
//
//    public List<LessonDto> getLessons() {
//        return lessons;
//    }

    @Override
    public String toString() {
        return "CourseDto{" +
                "subCategoryId=" + subCategoryId +
                ", image=" + image +
                ", courseDescription='" + courseDescription + '\'' +
                ", courseName='" + courseName + '\'' +
                ", courseId=" + courseId +
                '}';
    }
}
