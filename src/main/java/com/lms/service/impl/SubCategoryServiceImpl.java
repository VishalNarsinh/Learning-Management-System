package com.lms.service.impl;

import com.lms.dto.SubCategoryDto;
import com.lms.exception.ResourceNotFoundException;
import com.lms.mapper.CategoryMapper;
import com.lms.model.Category;
import com.lms.model.SubCategory;
import com.lms.repository.CategoryRepository;
import com.lms.repository.SubCategoryRepository;
import com.lms.service.CategoryService;
import com.lms.service.SubCategoryService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubCategoryServiceImpl implements SubCategoryService {
    private static final Logger log = LoggerFactory.getLogger(SubCategoryServiceImpl.class);
    private final ModelMapper modelMapper;
    private final CategoryService categoryService;
    private final SubCategoryRepository subCategoryRepository;
//    private final CourseService courseService;
    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;

    @Override
    public SubCategoryDto findSubCategoryBySubCategoryId(long subCategoryId) {
        SubCategory subCategory = subCategoryRepository.findById(subCategoryId).orElseThrow(() -> new ResourceNotFoundException("SubCategory", "subCategoryId", subCategoryId));
        return categoryMapper.toDto(subCategory);

    }

    @Override
    public SubCategoryDto saveSubCategory(SubCategoryDto subCategoryDto) {

        SubCategory subCategory = categoryMapper.toEntity(subCategoryDto);
        log.info("SubCategory {}", subCategory);
        subCategory.setCategory(categoryRepository.findById(subCategoryDto.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", subCategoryDto.getCategoryId())));
        return categoryMapper.toDto(subCategoryRepository.save(subCategory));
    }

    @Override
    @Transactional
    public SubCategoryDto updateSubCategory(SubCategoryDto subCategoryDto,long subCategoryId) {
        SubCategory subCategoryDB = subCategoryRepository.findById(subCategoryId).orElseThrow(() -> new ResourceNotFoundException("SubCategory", "subCategoryId", subCategoryId));
        Category category = categoryRepository.findById(subCategoryDto.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", subCategoryDto.getCategoryId()));
        subCategoryDB.setName(subCategoryDto.getName());
        subCategoryDB.setCategory(category);
        return categoryMapper.toDto(subCategoryRepository.save(subCategoryDB));
    }

    @Override
    public void deleteSubCategory(long subCategoryId) {
        SubCategory subCategory = subCategoryRepository.findById(subCategoryId).orElseThrow(() -> new ResourceNotFoundException("SubCategory", "subCategoryId", subCategoryId));
        subCategoryRepository.delete(subCategory);
    }

    @Override
    public List<SubCategoryDto> findSubCategoriesByCategoryId(long categoryId) {
        List<SubCategory> list = subCategoryRepository.findByCategoryCategoryId(categoryId);
        return list.stream().map(categoryMapper::toDto).toList();
    }

    @Override
    public List<SubCategoryDto> findAll() {
        return subCategoryRepository.findAll().stream().map(categoryMapper::toDto).toList();
    }
}
