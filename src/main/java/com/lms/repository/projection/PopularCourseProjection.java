package com.lms.repository.projection;

import com.lms.model.Course;

public interface PopularCourseProjection {
    Course getCourse();
    Long getEnrollmentCount();
}

