package com.lms.service.impl;

import com.lms.dto.CommentDto;
import com.lms.dto.CommentRequest;
import com.lms.exception.ResourceNotFoundException;
import com.lms.model.Comment;
import com.lms.model.Lesson;
import com.lms.model.User;
import com.lms.repository.CommentRepository;
import com.lms.repository.LessonRepository;
import com.lms.repository.UserRepository;
import com.lms.service.CommentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public CommentServiceImpl(LessonRepository lessonRepository, UserRepository userRepository, CommentRepository commentRepository) {
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public List<CommentDto> findCommentsByLessonId(Long lessonId) {
        return commentRepository.findByLesson_LessonId(lessonId).stream().map(this::toDto).toList();
    }

    @Override
    public CommentDto saveComment(CommentRequest commentRequest,String email) {
        Comment comment = new Comment();
        comment.setContent(commentRequest.getContent());
        Lesson lesson = lessonRepository.findById(commentRequest.getLessonId()).orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", commentRequest.getLessonId()));
        comment.setLesson(lesson);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        comment.setUser(user);
        Comment save = commentRepository.save(comment);
         return toDto(save);
    }

    private CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .userId(comment.getUser().getUserId())
                .content(comment.getContent())
                .lessonId(comment.getLesson().getLessonId())
                .userName(comment.getUser().getFirstName())
                .build();
    }

    @Override
    public void deleteComment(long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        commentRepository.delete(comment);
    }

    @Override
    public CommentDto findCommentById(Long commentId) {
        return toDto(commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId)));
    }
}
