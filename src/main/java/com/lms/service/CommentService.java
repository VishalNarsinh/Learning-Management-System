package com.lms.service;

import com.lms.dto.CommentDto;
import com.lms.dto.CommentRequest;

import java.util.List;

public interface CommentService {
    List<CommentDto> findCommentsByLessonId(Long lessongId);

    CommentDto saveComment(CommentRequest commentRequest,String email);

    void deleteComment(long commentId);

    CommentDto findCommentById(Long commentId);
}
