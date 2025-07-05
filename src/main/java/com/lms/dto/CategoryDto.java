package com.lms.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDto {
    private long categoryId;
    private String name;
//    private List<SubCategoryDto> subCategories=new ArrayList<>();
}
