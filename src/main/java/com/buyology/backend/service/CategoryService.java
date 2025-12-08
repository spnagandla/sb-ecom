package com.buyology.backend.service;

import com.buyology.backend.dto.CategoryDTO;
import com.buyology.backend.dto.response.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {

    CategoryResponseDTO getAllCategories(Integer pageNumber, Integer pageSize);
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    CategoryDTO deleteCategory(Long categoryId);
    CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO);
    void deleteAllCategory();
    List<CategoryDTO> createBulkCategories(List<CategoryDTO> categoryDTO);
}
