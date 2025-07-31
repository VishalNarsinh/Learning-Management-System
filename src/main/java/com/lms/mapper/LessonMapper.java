package com.lms.mapper;

import com.lms.dto.CommentDto;
import com.lms.dto.LessonDto;
import com.lms.model.Lesson;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class LessonMapper {

    public Lesson toEntity(LessonDto dto) {
        if (dto == null) return null;

        Lesson lesson = new Lesson();
        lesson.setLessonName(dto.getLessonName());
        lesson.setLessonContent(dto.getLessonContent());
        lesson.setImage(dto.getImage());
        lesson.setVideo(dto.getVideo());
//        lesson.setComments(dto.getComments());

        return lesson;
    }

    public LessonDto toDto(Lesson lesson) {
        if (lesson == null) return null;

        LessonDto dto = new LessonDto();
        dto.setLessonId(lesson.getLessonId());
        dto.setLessonName(lesson.getLessonName());
        dto.setLessonContent(lesson.getLessonContent());
        dto.setSequenceNumber(lesson.getSequenceNumber());
        dto.setImage(lesson.getImage());
        dto.setVideo(lesson.getVideo());
        dto.setCourseId(lesson.getCourse() != null ? lesson.getCourse().getCourseId() : null);
        dto.setComments(
                lesson.getComments().stream().map(comment -> CommentDto.builder()
                            .commentId(comment.getCommentId())
                            .content(comment.getContent())
                            .userName(comment.getUser() != null ? comment.getUser().getFirstName() : null)
                            .userId(comment.getUser() != null ? comment.getUser().getUserId() : null)
                            .build()
                ).collect(Collectors.toList())
        );
        return dto;
    }
}
