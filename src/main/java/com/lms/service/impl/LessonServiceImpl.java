package com.lms.service.impl;

import com.lms.dto.LessonDto;
import com.lms.exception.ResourceNotFoundException;
import com.lms.mapper.LessonMapper;
import com.lms.model.Image;
import com.lms.model.Lesson;
import com.lms.model.Video;
import com.lms.repository.CourseRepository;
import com.lms.repository.LessonProgressRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {
    private static final Logger log = LoggerFactory.getLogger(LessonServiceImpl.class);
    private final LessonMapper lessonMapper;
    private final CourseRepository courseRepository;
    private final ImageService imageService;
    private final VideoService videoService;
    private final LessonRepository lessonRepository;
    private final Executor lessonTaskExecutor;
    private final LessonProgressRepository lessonProgressRepository;


    @Override
    public LessonDto saveLesson(LessonDto lessonDto, MultipartFile imageFile, MultipartFile videoFile) throws IOException {
        Lesson entity = lessonMapper.toEntity(lessonDto);
        entity.setCourse(courseRepository.findById(lessonDto.getCourseId()).orElseThrow(() -> new ResourceNotFoundException("Course", "id", lessonDto.getCourseId())));
        int nextSequence = lessonRepository.getMaxSequenceNumberByCourseId(lessonDto.getCourseId()) + 1;
        entity.setSequenceNumber(nextSequence);
        Image image = imageService.uploadImage(imageFile, "lesson");
        log.info("Image Id : {}", image.getImageId());
        entity.setImage(image);
        Video video = videoService.saveVideo(videoFile);
        log.info("Video Id : {}", video.getVideoId());
        entity.setVideo(video);
        log.info("{}", entity);
        return lessonMapper.toDto(lessonRepository.save(entity));
    }

    @Override
    public CompletableFuture<LessonDto> saveLessonAsync(LessonDto lessonDto, MultipartFile imageFile, MultipartFile videoFile) throws IOException, ExecutionException, InterruptedException {
        Lesson entity = lessonMapper.toEntity(lessonDto);
        entity.setCourse(courseRepository.findById(lessonDto.getCourseId()).orElseThrow(() -> new ResourceNotFoundException("Course", "id", lessonDto.getCourseId())));
        int nextSequence = lessonRepository.getMaxSequenceNumberByCourseId(lessonDto.getCourseId()) + 1;
        entity.setSequenceNumber(nextSequence);
        CompletableFuture<Image> imageFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return imageService.uploadImage(imageFile, "lesson");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, lessonTaskExecutor);
        CompletableFuture<Video> videoFuture = CompletableFuture.supplyAsync(() -> videoService.saveVideo(videoFile), lessonTaskExecutor);
        return imageFuture.thenCombine(videoFuture, (image, video) -> {
                    entity.setImage(image);
                    entity.setVideo(video);
                    return lessonRepository.save(entity);
                })
                .thenApply(lessonMapper::toDto)
                .exceptionally(ex -> {
                    log.error("{}", ex);
                    throw new RuntimeException(ex);
                });

    }

    @Override
    public LessonDto updateLesson(long lessonId, LessonDto lessonDto, MultipartFile imageFile, MultipartFile videoFile, String instructorEmail) throws IOException {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        if (!lesson.getCourse().getInstructor().getEmail().equals(instructorEmail))
            throw new AccessDeniedException("You are not allowed to update this lesson");
        if (imageFile != null && !imageFile.isEmpty()) {
            long imageId = lesson.getImage().getImageId();
            lesson.setImage(null);

            imageService.deleteImage(imageId);
            Image image = imageService.uploadImage(imageFile, "lesson");
            lesson.setImage(image);
            log.info("{}", image);
        }
        if (videoFile != null && !videoFile.isEmpty()) {
            long videoId = lesson.getVideo().getVideoId();
            lesson.setVideo(null);
            videoService.deleteVideo(videoId);
            Video video = videoService.saveVideo(videoFile);
            lesson.setVideo(video);
            log.info("{}", video);
        }
        lesson.setLessonName(lessonDto.getLessonName());
        lesson.setLessonContent(lessonDto.getLessonContent());
        return lessonMapper.toDto(lessonRepository.save(lesson));
    }




    @Override
    public CompletableFuture<LessonDto> updateLessonAsync(long lessonId, LessonDto lessonDto, MultipartFile imageFile, MultipartFile videoFile, String instructorEmail) throws IOException {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        if (!lesson.getCourse().getInstructor().getEmail().equals(instructorEmail))
            throw new AccessDeniedException("You are not allowed to update this lesson");
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        if (imageFile != null && !imageFile.isEmpty()) {
            futures.add(updateImage(lesson, imageFile).thenAccept(updated -> {}));
        }

        if (videoFile != null && !videoFile.isEmpty()) {
            futures.add(updateVideo(lesson, videoFile).thenAccept(updated -> {}));
        }

        // Run image/video updates in parallel
        CompletableFuture<Void> combined = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        return combined.thenApply(v -> {
                    lesson.setLessonName(lessonDto.getLessonName());
                    lesson.setLessonContent(lessonDto.getLessonContent());
                    return lessonRepository.save(lesson);
                })
                .thenApply(lessonMapper::toDto)
                .exceptionally(ex -> {
                    log.error("Failed to update lesson {}", ex.getMessage(), ex);
                    throw new RuntimeException(ex);
                });
    }



    public CompletableFuture<LessonDto> updateLessonAsync1(long lessonId, LessonDto lessonDto, MultipartFile imageFile, MultipartFile videoFile, String instructorEmail) throws IOException {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        if (!lesson.getCourse().getInstructor().getEmail().equals(instructorEmail))
            throw new AccessDeniedException("You are not allowed to update this lesson");

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            if (imageFile != null && !imageFile.isEmpty()) {
                long imageId = lesson.getImage().getImageId();
                lesson.setImage(null);
                imageService.deleteImage(imageId);
                Image image;
                try {
                    image = imageService.uploadImage(imageFile, "lesson");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                lesson.setImage(image);
                log.info("{}", image);
            }
        }, lessonTaskExecutor);

        CompletableFuture<Void> videoFuture = CompletableFuture.runAsync(() -> {
            if (videoFile != null && !videoFile.isEmpty()) {
                long videoId = lesson.getVideo().getVideoId();
                lesson.setVideo(null);
                videoService.deleteVideo(videoId);
                Video video = videoService.saveVideo(videoFile);
                lesson.setVideo(video);
                log.info("{}", video);
            }
        }, lessonTaskExecutor);

        return imageFuture.thenCombine(videoFuture, (image, video) -> {
                    lesson.setLessonName(lessonDto.getLessonName());
                    lesson.setLessonContent(lessonDto.getLessonContent());
                    return lessonRepository.save(lesson);
                })
                .thenApply(lessonMapper::toDto)
                .exceptionally(ex -> {
                    log.error("{}", ex);
                    throw new RuntimeException(ex);
                });


    }

    @Override
    public CompletableFuture<Void> updateImage(Lesson lesson, MultipartFile file) {
        return CompletableFuture.runAsync(() -> {
            try {
                long oldImageId = lesson.getImage().getImageId();
                lesson.setImage(null);
                lessonRepository.save(lesson);
                imageService.deleteImage(oldImageId);
                lesson.setImage(imageService.uploadImage(file, "lesson"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, lessonTaskExecutor);
    }

    @Override
    public CompletableFuture<Void> updateVideo(Lesson lesson, MultipartFile file) {
        return CompletableFuture.runAsync(() -> {
            try {
                long oldVideoId = lesson.getVideo().getVideoId();
                lesson.setVideo(null);
                lessonRepository.save(lesson);
                videoService.deleteVideo(oldVideoId);
                lesson.setVideo(videoService.saveVideo(file));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, lessonTaskExecutor);
    }


    @Transactional
    @Override
    public void deleteLesson(long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        long imageId = lesson.getImage().getImageId();
        long videoId = lesson.getVideo().getVideoId();
        lesson.setImage(null);
        lesson.setVideo(null);
        imageService.deleteImage(imageId);
        videoService.deleteVideo(videoId);
//        lessonRepository.saveAndFlush(lesson);
        lessonProgressRepository.deleteLessonProgressesByLesson_LessonId(lessonId);
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

    @Override
    public void updateLessonSequence(Long courseId, Long lessonId, int newPosition) {
        Lesson lessonToMove = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

        int currentPosition = lessonToMove.getSequenceNumber();

        if (newPosition == currentPosition) return;

        // Shift other lessons
        if (newPosition < currentPosition) {
            // Move others down: e.g., 2 → 3, 3 → 4, etc.
            List<Lesson> toShiftDown = lessonRepository.findByCourse_CourseIdAndSequenceNumberBetweenOrderBySequenceNumberAsc(
                    courseId, newPosition, currentPosition - 1);

            for (Lesson l : toShiftDown) {
                l.setSequenceNumber(l.getSequenceNumber() + 1);
            }

            lessonRepository.saveAll(toShiftDown);
        } else {
            // Move others up: e.g., 6 → 5, 5 → 4, etc.
            List<Lesson> toShiftUp = lessonRepository.findByCourse_CourseIdAndSequenceNumberBetweenOrderBySequenceNumberAsc(
                    courseId, currentPosition + 1, newPosition);

            for (Lesson l : toShiftUp) {
                l.setSequenceNumber(l.getSequenceNumber() - 1);
            }

            lessonRepository.saveAll(toShiftUp);
        }

        // Update the target lesson
        lessonToMove.setSequenceNumber(newPosition);
        lessonRepository.save(lessonToMove);
    }

}
