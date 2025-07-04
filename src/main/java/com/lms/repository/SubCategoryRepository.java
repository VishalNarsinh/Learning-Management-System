package com.lms.repository;

import com.lms.model.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubCategoryRepository extends JpaRepository<SubCategory,Long> {
    List<SubCategory> findByCategoryCategoryId(Long subCategoryId);

    Optional<SubCategory> findByName(String name);
}
