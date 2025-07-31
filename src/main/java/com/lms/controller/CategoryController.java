package com.lms.controller;

import com.lms.dto.CategoryDto;
import com.lms.dto.CustomMessage;
import com.lms.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/")
    public ResponseEntity<?> saveCategory(@RequestBody CategoryDto categoryDto) {
        return ResponseEntity.ok(categoryService.saveCategory(categoryDto));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<?> getCategory(@PathVariable long categoryId) {
        return ResponseEntity.ok(categoryService.findCategoryByCategoryId(categoryId));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<?> updateCategory(@RequestBody CategoryDto categoryDto,@PathVariable long categoryId) {
        return ResponseEntity.ok(categoryService.updateCategory(categoryDto,categoryId));
    }

    @DeleteMapping("/{categoryId}")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(CustomMessage.builder().message("Category deleted successfully").status("success").build());
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(categoryService.findAll());
    }
}
