package com.lms.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentDto {
    private long commentId;
    private String content;
    private Long userId;
    private String userName; // if needed for display
}

