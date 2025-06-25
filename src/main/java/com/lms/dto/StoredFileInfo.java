package com.lms.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoredFileInfo {

    private String originalFileName;  // e.g., LectureNotes.pdf
    private String objectName;        // e.g., uuid-LectureNotes.pdf
    private String fileUrl;           // e.g., uploads/resources/uuid-LectureNotes.pdf
    private String contentType;       // e.g., application/pdf or image/png
}
