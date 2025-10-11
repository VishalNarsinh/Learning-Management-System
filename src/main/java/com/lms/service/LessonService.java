package com.lms.service;

import com.lms.dto.LessonDto;
import com.lms.model.Lesson;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface LessonService {


    LessonDto saveLesson(LessonDto lessonDto, MultipartFile imageFile, MultipartFile videoFile) throws IOException;

    CompletableFuture<LessonDto> saveLessonAsync(LessonDto lessonDto, MultipartFile imageFile, MultipartFile videoFile) throws IOException, ExecutionException, InterruptedException;

    LessonDto updateLesson(long lessonId, LessonDto lessonDto, MultipartFile imageFile, MultipartFile videoFile, String instructorEmail) throws IOException;

    CompletableFuture<LessonDto> updateLessonAsync(long lessonId, LessonDto lessonDto, MultipartFile imageFile, MultipartFile videoFile, String instructorEmail) throws IOException;

    CompletableFuture<LessonDto> updateLessonAsync1(long lessonId, LessonDto lessonDto, MultipartFile imageFile, MultipartFile videoFile, String instructorEmail) throws IOException;

    CompletableFuture<Void> updateImage(Lesson lesson, MultipartFile file);

    CompletableFuture<Void> updateVideo(Lesson lesson, MultipartFile file);

    void deleteLesson(long lessonId);

    LessonDto findLessonById(long lessonId);
    List<LessonDto> findLessonsByCourseId(long courseId);

    void updateLessonSequence(Long courseId, Long lessonId, int newPosition);

    void bulkUploadLessons(List<LessonDto> lessonDtos);
}
