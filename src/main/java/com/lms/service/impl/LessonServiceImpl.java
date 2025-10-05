package com.lms.service.impl;

import com.lms.dto.LessonDto;
import com.lms.exception.ResourceNotFoundException;
import com.lms.exception.SamePositionException;
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
import com.lms.utils.AppConstants;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
        long start = System.currentTimeMillis();
        Lesson entity = lessonMapper.toEntity(lessonDto);
        entity.setCourse(courseRepository.findById(lessonDto.getCourseId()).orElseThrow(() -> new ResourceNotFoundException("Course", "id", lessonDto.getCourseId())));
        int nextSequence = lessonRepository.getMaxSequenceNumberByCourseId(lessonDto.getCourseId()) + 1;
        entity.setSequenceNumber(nextSequence);
        if (imageFile != null && !imageFile.isEmpty()) {
            Image image = imageService.uploadImage(imageFile, AppConstants.LESSON_IMAGE_FOLDER);
            log.info("Image Id : {}", image.getImageId());
            entity.setImage(image);
        }
        if (videoFile != null && !videoFile.isEmpty()) {
            Video video = videoService.saveVideo(videoFile);
            log.info("Video Id : {}", video.getVideoId());
            entity.setVideo(video);
        }

        log.info("{}", entity);
        log.info("Total Time : {}", System.currentTimeMillis() - start);
        return lessonMapper.toDto(lessonRepository.save(entity));
    }

    @Override
    public CompletableFuture<LessonDto> saveLessonAsync(LessonDto lessonDto, MultipartFile imageFile, MultipartFile videoFile) throws IOException, ExecutionException, InterruptedException {
        Lesson entity = lessonMapper.toEntity(lessonDto);
        entity.setCourse(courseRepository.findById(lessonDto.getCourseId()).orElseThrow(() -> new ResourceNotFoundException("Course", "id", lessonDto.getCourseId())));
        int nextSequence = lessonRepository.getMaxSequenceNumberByCourseId(lessonDto.getCourseId()) + 1;
        entity.setSequenceNumber(nextSequence);
        CompletableFuture<Image> imageFuture = (imageFile != null && !imageFile.isEmpty()) ? CompletableFuture.supplyAsync(() -> {
            try {
                return imageService.uploadImage(imageFile, AppConstants.LESSON_IMAGE_FOLDER);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, lessonTaskExecutor) : CompletableFuture.completedFuture(null);
        CompletableFuture<Video> videoFuture =  (videoFile != null && !videoFile.isEmpty()) ? CompletableFuture.supplyAsync(() -> videoService.saveVideo(videoFile), lessonTaskExecutor): CompletableFuture.completedFuture(null);
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
            Image oldImage = lesson.getImage();
            if (oldImage != null) {
                lesson.setImage(null);
                imageService.deleteImage(oldImage.getImageId());
            }
            Image newImage = imageService.uploadImage(imageFile, AppConstants.LESSON_IMAGE_FOLDER);
            lesson.setImage(newImage);
            log.info("{}", newImage);
        }
        videoHelper(videoFile, lesson);
        lesson.setLessonName(lessonDto.getLessonName());
        lesson.setLessonContent(lessonDto.getLessonContent());
        return lessonMapper.toDto(lessonRepository.save(lesson));
    }

    private void videoHelper(MultipartFile videoFile, Lesson lesson) {
        if (videoFile != null && !videoFile.isEmpty()) {
            Video oldVideo = lesson.getVideo();
            if (oldVideo != null) {
                lesson.setVideo(null);
                lessonRepository.save(lesson);
                videoService.deleteVideo(oldVideo.getVideoId());
            }
            Video newVideo = videoService.saveVideo(videoFile);
            lesson.setVideo(newVideo);
            log.info("{}", newVideo);
        }
    }

    @Override
    public CompletableFuture<LessonDto> updateLessonAsync(long lessonId, LessonDto lessonDto, MultipartFile imageFile, MultipartFile videoFile, String instructorEmail) throws IOException {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        if (!lesson.getCourse().getInstructor().getEmail().equals(instructorEmail))
            throw new AccessDeniedException("You are not allowed to update this lesson");
       lesson.setLessonName(lessonDto.getLessonName());
       lesson.setLessonContent(lessonDto.getLessonContent());
        CompletableFuture<Void> imageFuture = (imageFile != null && !imageFile.isEmpty()) ?updateImage(lesson,imageFile): CompletableFuture.completedFuture(null);
        CompletableFuture<Void> videoFuture =  (videoFile != null && !videoFile.isEmpty()) ?updateVideo(lesson,videoFile): CompletableFuture.completedFuture(null);
        return CompletableFuture.allOf(imageFuture, videoFuture)
                .thenApply(voidResult -> lessonRepository.save(lesson))
                .thenApply(lessonMapper::toDto);
    }

    @Override
    public CompletableFuture<LessonDto> updateLessonAsync1(long lessonId, LessonDto lessonDto, MultipartFile imageFile, MultipartFile videoFile, String instructorEmail) throws IOException {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));
        if (!lesson.getCourse().getInstructor().getEmail().equals(instructorEmail))
            throw new AccessDeniedException("You are not allowed to update this lesson");

        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            if (imageFile != null && !imageFile.isEmpty()) {
                Image oldImage = lesson.getImage();
                if (oldImage != null) {
                    lesson.setImage(null);
                    lessonRepository.save(lesson);
                    imageService.deleteImage(oldImage.getImageId());
                }
                try {
                    lesson.setImage( imageService.uploadImage(imageFile, AppConstants.LESSON_IMAGE_FOLDER));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, lessonTaskExecutor);

        CompletableFuture<Void> videoFuture = CompletableFuture.runAsync(() -> {
            videoHelper(videoFile, lesson);
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
                Image oldImage = lesson.getImage();
                if (oldImage != null) {
                    lesson.setImage(null);
                    lessonRepository.save(lesson);
                    imageService.deleteImage(oldImage.getImageId());
                }
                lesson.setImage( imageService.uploadImage(file, AppConstants.LESSON_IMAGE_FOLDER));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, lessonTaskExecutor);
    }

    @Override
    public CompletableFuture<Void> updateVideo(Lesson lesson, MultipartFile file) {
        return CompletableFuture.runAsync(() -> {
            Video oldVideo = lesson.getVideo();
            if (oldVideo != null) {
                lesson.setVideo(null);
                lessonRepository.save(lesson);
                videoService.deleteVideo(oldVideo.getVideoId());
            }
            Video newVideo = videoService.saveVideo(file);
            lesson.setVideo(newVideo);
            log.info("{}", newVideo);
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
        List<LessonDto> list = lessonRepository.findByCourseCourseIdOrderBySequenceNumber(courseId).stream().map(lessonMapper::toDto).toList();
        if(list.isEmpty()) throw new ResourceNotFoundException("Lesson", "courseId", courseId);
        return list;
    }


   @Override
   @Transactional
   public void updateLessonSequence(Long courseId, Long lessonId, int newPosition) {
       Lesson lessonToMove = lessonRepository.findById(lessonId)
               .orElseThrow(() -> new ResourceNotFoundException("Lesson", "id", lessonId));

       int currentPosition = lessonToMove.getSequenceNumber();
       if (newPosition == currentPosition) {
           throw new SamePositionException("Lesson is already at this position");
       }

       // Step 1: Temporarily nullify the moving lesson
       lessonToMove.setSequenceNumber(null);
       lessonRepository.save(lessonToMove);

       // Step 2: Shift other lessons
       if (newPosition < currentPosition) {
           lessonRepository.bulkShift(courseId, newPosition, currentPosition - 1, 1); // shift down
       } else {
           lessonRepository.bulkShift(courseId, currentPosition + 1, newPosition, -1); // shift up
       }

       // Step 3: Move the lesson to the new position
       lessonToMove.setSequenceNumber(newPosition);
       lessonRepository.save(lessonToMove);
   }


}
