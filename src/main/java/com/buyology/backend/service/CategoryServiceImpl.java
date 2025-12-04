package com.buyology.backend.service;

import com.buyology.backend.exception.APIException;
import com.buyology.backend.exception.ResourceNotFoundException;
import com.buyology.backend.model.Category;
import com.buyology.backend.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Override
    public List<Category> getAllCategories() {
        log.info("Fetching all categories");
        List<Category> categories = categoryRepository.findAll();
        if(categories.isEmpty()) throw new APIException("No categories Found");
        return categories;
    }

    @Override
    public Category createCategory(Category category) {
        log.info("Request to create category: {}", category);
        Category savedCategory = categoryRepository.findByCategoryName(category.getCategoryName());
        if(savedCategory!= null) throw new APIException("Category with name " + category.getCategoryName() + " already exists!!");
        categoryRepository.save(category);
        log.info("Created category with id: {}", category.getCategoryId());
        return category;
    }

    @Override
    public void deleteCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category","categroyId",categoryId);
        }
        categoryRepository.deleteById(categoryId);
        log.info("Category with id:{} deleted successfully", categoryId);
    }

    @Override
    public Category updateCategory(Long categoryId, Category categoryRequest) {

        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category","categroyId",categoryId));
        existingCategory.setCategoryName(categoryRequest.getCategoryName());
        return categoryRepository.save(existingCategory);
    }

}
