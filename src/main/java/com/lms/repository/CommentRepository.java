package com.lms.repository;

import com.lms.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findByLesson_LessonId(Long lessonLessonId);
}
