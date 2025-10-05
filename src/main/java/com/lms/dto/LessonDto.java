package com.lms.dto;

import com.lms.model.Image;
import com.lms.model.Video;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LessonDto {
    private long lessonId;
    private String lessonName;
    private String lessonContent;
    private Integer sequenceNumber;
    private Image image;
    private Video video;
    private Long courseId;
    private List<CommentDto> comments=new ArrayList<>();
}
