package com.lms.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubCategoryDto {
    private long subCategoryId;
    private String name;
    private Long categoryId;
//    private List<CourseDto> courses=new ArrayList<>();
}
