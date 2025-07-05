package com.lms.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PopularCourseDto {
    private CourseDto course;
    private long enrollmentCount;
    private double averageRating;
}
