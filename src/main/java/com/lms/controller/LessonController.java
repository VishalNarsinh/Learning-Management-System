package com.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.CustomMessage;
import com.lms.dto.LessonDto;
import com.lms.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
public class LessonController {
    private static final Logger log = LoggerFactory.getLogger(LessonController.class);
    private final LessonService lessonService;
    private final ObjectMapper objectMapper;

    @PostMapping("/")
    public ResponseEntity<?> saveLesson(@RequestParam("lesson") String lessonData,@RequestParam("image") MultipartFile imageFile, @RequestParam("video") MultipartFile videoFile) throws Exception {
        LessonDto lessonDto = objectMapper.readValue(lessonData, LessonDto.class);
        log.info("{}",lessonDto);
        LessonDto saved = lessonService.saveLesson(lessonDto, imageFile, videoFile);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/async")
    public ResponseEntity<?> saveLessonAsync(@RequestParam("lesson") String lessonData,@RequestParam("image") MultipartFile imageFile, @RequestParam("video") MultipartFile videoFile) throws Exception {
        LessonDto lessonDto = objectMapper.readValue(lessonData, LessonDto.class);
        log.info("{}",lessonDto);
        LessonDto saved = lessonService.saveLessonAsync(lessonDto, imageFile, videoFile).get();
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
    public ResponseEntity<?> updateLesson(@PathVariable long lessonId, @RequestParam("lesson") String lessonDtoData, @RequestParam(value = "image",required = false) MultipartFile imageFile, @RequestParam(value = "video",required = false)MultipartFile videoFile, Principal principal) throws IOException, ExecutionException, InterruptedException {
        LessonDto lessonDto = objectMapper.readValue(lessonDtoData, LessonDto.class);
        return ResponseEntity.ok(lessonService.updateLesson(lessonId,lessonDto,imageFile,videoFile,principal.getName()));
    }

    @PutMapping("/async/{lessonId}")
    public ResponseEntity<?> updateLessonAsync(@PathVariable long lessonId, @RequestParam("lesson") String lessonDtoData, @RequestParam(value = "image",required = false) MultipartFile imageFile, @RequestParam(value = "video",required = false)MultipartFile videoFile, Principal principal) throws IOException, ExecutionException, InterruptedException {
        LessonDto lessonDto = objectMapper.readValue(lessonDtoData, LessonDto.class);
        return ResponseEntity.ok(lessonService.updateLessonAsync(lessonId,lessonDto,imageFile,videoFile,principal.getName()).get());
    }

    @PutMapping("/async1/{lessonId}")
    public ResponseEntity<?> updateLessonAsync1(@PathVariable long lessonId, @RequestParam("lesson") String lessonDtoData, @RequestParam(value = "image",required = false) MultipartFile imageFile, @RequestParam(value = "video",required = false)MultipartFile videoFile, Principal principal) throws IOException, ExecutionException, InterruptedException {
        LessonDto lessonDto = objectMapper.readValue(lessonDtoData, LessonDto.class);
        return ResponseEntity.ok(lessonService.updateLessonAsync1(lessonId,lessonDto,imageFile,videoFile,principal.getName()).get());
    }

    @PatchMapping("/{lessonId}/sequence")
    public ResponseEntity<String> updateLessonSequence(
            @PathVariable Long lessonId,
            @RequestParam Long courseId,
            @RequestParam int newPosition) {

        lessonService.updateLessonSequence(courseId, lessonId, newPosition);
        return ResponseEntity.ok("Lesson sequence updated.");
    }


    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getLessonsByCourseId(@PathVariable long courseId) {
        return ResponseEntity.ok(lessonService.findLessonsByCourseId(courseId));
    }
}
