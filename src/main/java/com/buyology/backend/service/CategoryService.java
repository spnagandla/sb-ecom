package com.buyology.backend.service;

import com.buyology.backend.model.Category;

import java.util.List;

public interface CategoryService {

    List<Category> getAllCategories();
    Category createCategory(Category category);
    void deleteCategory(Long categoryId);
    Category updateCategory(Long categoryId, Category category);
}
