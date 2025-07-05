package com.lms.repository.projection;

import com.lms.model.Course;

public interface PopularCourseWithRatingProjection {
    Course getCourse();
    Long getEnrollmentCount();
    Double getAverageRating();
}
