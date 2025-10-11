package com.lms.controller;

import com.lms.dto.CommentRequest;
import com.lms.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<?> createComment(@RequestBody CommentRequest commentRequest, Principal principal) {
        commentService.saveComment(commentRequest,principal.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<?> getCommentById(@PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.findCommentById(commentId));
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<?> getCommentsByLessonId(@PathVariable Long lessonId) {
        return ResponseEntity.ok(commentService.findCommentsByLessonId(lessonId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }


}
