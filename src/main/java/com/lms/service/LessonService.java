package com.lms.service;

import com.lms.dto.LessonDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface LessonService {


    LessonDto saveLesson(LessonDto lessonDto, MultipartFile imageFile, MultipartFile videoFile) throws IOException;

    LessonDto updateLesson(long lessonId, LessonDto lessonDto,MultipartFile imageFile, MultipartFile videoFile,String instructorEmail) throws IOException;

    void deleteLesson(long lessonId);

    LessonDto findLessonById(long lessonId);
    List<LessonDto> findLessonsByCourseId(long courseId);
}
