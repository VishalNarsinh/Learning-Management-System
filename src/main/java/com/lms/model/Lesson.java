package com.lms.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lessons",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"course_id","sequence_number"})
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_id")
    private long lessonId;

    @Column(length = 80, nullable = false)
    private String lessonName;

    private String lessonContent;

    @Column(name = "sequence_number", nullable = true)
    private Integer sequenceNumber;


    @OneToOne
    @JoinColumn(name = "image_id")
    private Image image;

    @OneToOne
    @JoinColumn(name = "video_id")
    private Video video;

    @ManyToOne
    @JoinColumn(name = "course_id",nullable = false)
    private Course course;


    @OneToMany(mappedBy = "lesson", cascade = CascadeType.REMOVE,orphanRemoval = true,fetch = FetchType.EAGER)
    private List<Comment> comments = new ArrayList<>();

    @Override
    public String toString() {
        return "Lesson{" +
                "video=" + video +
                ", image=" + image +
                ", lessonContent='" + lessonContent + '\'' +
                ", lessonName='" + lessonName + '\'' +
                ", lessonId=" + lessonId +
                '}';
    }
}
