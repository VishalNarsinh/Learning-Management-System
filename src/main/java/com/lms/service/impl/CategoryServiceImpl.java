package com.lms.service.impl;

import com.google.common.base.Optional;
import com.lms.dto.CategoryDto;
import com.lms.exception.DuplicateResourceException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.mapper.CategoryMapper;
import com.lms.model.Category;
import com.lms.repository.CategoryRepository;
import com.lms.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;



    @Override
    public CategoryDto saveCategory(CategoryDto categoryDto) {
        String normalizedName = categoryDto.getName().toLowerCase(Locale.ROOT);
        if(categoryRepository.findByName(normalizedName).isPresent()){
            throw new DuplicateResourceException("Category name already exists.");
        }
        Category category = categoryMapper.toEntity(categoryDto);
        category.setName(normalizedName);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public CategoryDto updateCategory(CategoryDto categoryDto,long categoryId) {
        String normalizedName = categoryDto.getName().toLowerCase(Locale.ROOT);
        Optional<Category> existingCategory = categoryRepository.findByName(normalizedName);
        if(existingCategory.isPresent() && existingCategory.get().getCategoryId() != categoryId){
            throw new DuplicateResourceException("Category name already exists.");
        }
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryDto.getCategoryId()));
        category.setName(normalizedName);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        categoryRepository.delete(category);
    }

    @Override
    public CategoryDto findCategoryByCategoryId(long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category", "id",categoryId));
        return categoryMapper.toDto(category);}

    @Override
    public List<CategoryDto> findAll() {
        Sort sort =  Sort.by(Sort.Direction.ASC, "name");
        return categoryRepository.findAll(sort).stream().map(categoryMapper::toDto).toList();
    }
}
