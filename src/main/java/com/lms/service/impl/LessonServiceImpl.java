package com.lms.service.impl;

import com.lms.dto.LessonDto;
import com.lms.exception.ResourceNotFoundException;
import com.lms.mapper.LessonMapper;
import com.lms.model.Image;
import com.lms.model.Lesson;
import com.lms.model.Video;
import com.lms.repository.CourseRepository;
import com.lms.repository.LessonRepository;
import com.lms.service.ImageService;
import com.lms.service.LessonService;
import com.lms.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {
    private static final Logger log = LoggerFactory.getLogger(LessonServiceImpl.class);
    private final LessonMapper lessonMapper;
    private final CourseRepository courseRepository;
    private final ImageService imageService;
    private final VideoService videoService;
    private final LessonRepository lessonRepository;

    @Override
    public LessonDto saveLesson(LessonDto lessonDto, MultipartFile imageFile, MultipartFile videoFile) throws IOException {
        Lesson entity = lessonMapper.toEntity(lessonDto);
        entity.setCourse(courseRepository.findById(lessonDto.getCourseId()).orElseThrow(()->new ResourceNotFoundException("Course","id",lessonDto.getCourseId())));
        Image image = imageService.uploadImage(imageFile, "lesson");
        log.info("Image Id : {}", image.getImageId());
        entity.setImage(image);
        Video video = videoService.saveVideo(videoFile);
        log.info("Video Id : {}", video.getVideoId());
        entity.setVideo(video);
        return lessonMapper.toDto(lessonRepository.save(entity));
    }

    @Override
    public LessonDto updateLesson(long lessonId, LessonDto lessonDto, MultipartFile imageFile, MultipartFile videoFile, String instructorEmail) throws IOException {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        if(!lesson.getCourse().getInstructor().getEmail().equals(instructorEmail))
            throw new AccessDeniedException("You are not allowed to update this lesson");
        if(imageFile != null && !imageFile.isEmpty()) {
            long imageId = lesson.getImage().getImageId();
            lesson.setImage(null);
            imageService.deleteImage(imageId);
            Image image = imageService.uploadImage(imageFile, "lesson");
            lesson.setImage(image);
            log.info("{}",image);
        }
        if(videoFile != null && !videoFile.isEmpty()) {
            long videoId = lesson.getVideo().getVideoId();
            lesson.setVideo(null);
            videoService.deleteVideo(videoId);
            Video video = videoService.saveVideo(videoFile);
            lesson.setVideo(video);
            log.info("{}",video);
        }
        lesson.setLessonName(lessonDto.getLessonName());
        lesson.setLessonContent(lessonDto.getLessonContent());
        return lessonMapper.toDto(lessonRepository.save(lesson));
    }


    @Transactional
    @Override
    public void deleteLesson(long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        imageService.deleteImage(lesson.getImage().getImageId());
        videoService.deleteVideo(lesson.getVideo().getVideoId());
        lesson.setImage(null);
        lesson.setVideo(null);
//        lessonRepository.saveAndFlush(lesson);
        lessonRepository.delete(lesson);
    }

    @Override
    public LessonDto findLessonById(long lessonId) {
        return lessonRepository.findById(lessonId).map(lessonMapper::toDto).orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
    }

    @Override
    public List<LessonDto> findLessonsByCourseId(long courseId) {
        return lessonRepository.findByCourseCourseId(courseId).stream().map(lessonMapper::toDto).toList();
    }
}
