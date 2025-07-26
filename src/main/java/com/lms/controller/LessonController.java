package com.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.CustomMessage;
import com.lms.dto.LessonDto;
import com.lms.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
public class LessonController {
    private final LessonService lessonService;
    private final ObjectMapper objectMapper;

    @PostMapping("/")
    public ResponseEntity<?> saveLesson(@RequestParam("lesson") String lessonData,@RequestParam("image") MultipartFile imageFile, @RequestParam("video") MultipartFile videoFile) throws Exception {
        LessonDto lessonDto = objectMapper.readValue(lessonData, LessonDto.class);
        LessonDto saved = lessonService.saveLesson(lessonDto, imageFile, videoFile);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<?> getLessonById(@PathVariable long lessonId) {
        return ResponseEntity.ok(lessonService.findLessonById(lessonId));
    }

    @DeleteMapping("/{lessonId}")
    public ResponseEntity<?> deleteLesson(@PathVariable long lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.ok(CustomMessage.builder().message("Lesson deleted successfully").status("success").build());
    }

    @PutMapping("/{lessonId}")
    public ResponseEntity<?> updateLesson(@PathVariable long lessonId, @RequestParam("lessonDto") String lessonDtoData, @RequestParam(value = "imageFile",required = false) MultipartFile imageFile, @RequestParam(value = "videoFile",required = false)MultipartFile videoFile, Principal principal) throws IOException {
        LessonDto lessonDto = objectMapper.readValue(lessonDtoData, LessonDto.class);
        return ResponseEntity.ok(lessonService.updateLesson(lessonId,lessonDto,imageFile,videoFile,principal.getName()));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getLessonsByCourseId(@PathVariable long courseId) {
        return ResponseEntity.ok(lessonService.findLessonsByCourseId(courseId));
    }
}
