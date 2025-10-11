package com.lms.dto;

import lombok.Data;

@Data
public class CommentRequest {
    private String content;
    private Long lessonId;
}
