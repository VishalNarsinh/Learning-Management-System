package com.lms.controller;

import com.lms.model.Video;
import com.lms.service.VideoService;
import com.lms.utils.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {

    private static final Logger log = LoggerFactory.getLogger(VideoController.class);
    private final VideoService videoService;

    @Value("${files.video.hls}")
    private String HLS_DIR;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @PostMapping("/save")
    public ResponseEntity<?> uploadVideo(
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(videoService.saveVideo(file));
    }

    @GetMapping("/stream/{videoId}")
    public ResponseEntity<Resource> getVideo(@PathVariable("videoId") long videoId) {
        Video video = videoService.getVideoByVideoId(videoId);
        String videoUrl = video.getVideoUrl();
        String contentType = video.getContentType();
        if (contentType == null || contentType.isEmpty())
            contentType = "application/octet-stream";
        Resource resource = new FileSystemResource(videoUrl);
        return ResponseEntity.
                ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);

    }

    @GetMapping("/stream/range/{videoId}")
    public ResponseEntity<Resource> getVideoRange(@PathVariable("videoId") long videoId,
                                                  @RequestHeader(name = "range", required = false) String range) throws IOException {
        if (range == null || range.isEmpty()) {
            return getVideo(videoId);
        }
        Video video = videoService.getVideoByVideoId(videoId);
        Path path = Paths.get(video.getVideoUrl());
        Resource resource = new FileSystemResource(video.getVideoUrl());
        String contentType = video.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            contentType = "application/octet-stream";
        }

        long fileLength = path.toFile().length();
        String[] ranges = range.replace("bytes=", "").split("-");
        long rangeStart = Long.parseLong(ranges[0]);

        long rangeEnd = rangeStart + AppConstants.CHUNK_SIZE - 1;
        if (rangeEnd > fileLength - 1) {
            rangeEnd = fileLength - 1;
        }

        try {
            InputStream inputStream = Files.newInputStream(path);
            inputStream.skip(rangeStart);
            long contentLength = rangeEnd - rangeStart + 1;
            log.info("rangeStart {} rangeEnd {} fileLength {} contentLength {}", rangeStart, rangeEnd, fileLength, contentLength);


            byte[] data = new byte[(int) contentLength];
            int read = inputStream.read(data, 0, data.length);
            log.info("read number of bytes{}", read);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
            httpHeaders.add("Cache-Control", "no-cache, no-store, must-revalidate");
            httpHeaders.add("Pragma", "no-cache");
            httpHeaders.add("Expires", "0");
            httpHeaders.add("X-Content-Type-Options", "nosniff");
            httpHeaders.setContentLength(contentLength);
            return ResponseEntity
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .headers(httpHeaders)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new ByteArrayResource(data));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/master/{videoId}/master.m3u8")
    public ResponseEntity<Resource> serveMasterFile(@PathVariable String videoId) {
        Path masterFilePath = Paths.get(HLS_DIR, videoId, "master.m3u8");
        if (!Files.exists(masterFilePath)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Resource resource = new FileSystemResource(masterFilePath);
        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl"
                )
                .body(resource);
    }

    @GetMapping("/master/{videoId}/{segment}")
    public ResponseEntity<Resource> serveSegments(@PathVariable String videoId, @PathVariable String segment) {

        Path path = Paths.get(HLS_DIR, videoId, segment);
        log.info(
                "Path {}",
                path
        );
        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Resource resource = new FileSystemResource(path);
        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_TYPE, "video/mp2t"
                )
                .body(resource);
    }
}
