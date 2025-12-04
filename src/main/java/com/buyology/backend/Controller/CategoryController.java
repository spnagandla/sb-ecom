package com.buyology.backend.Controller;

import com.buyology.backend.model.Category;
import com.buyology.backend.service.CategoryService;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;
    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

    public CategoryController(CategoryService categoryService){
        this.categoryService = categoryService;
    }

    @GetMapping("/public/categories")
    // Or @RequestMapping( value ="/public/categories", method = RequestMethod.GET)
    public ResponseEntity<List<Category>> getAllCategories(){
        log.info("Requesting for all the categories");
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/public/categories")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category){
        log.info("Requested to add a new Category");
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(category));
    }

    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId){
        log.info("requested to delete category with id:{}", categoryId);
       categoryService.deleteCategory(categoryId);
       return ResponseEntity.noContent().build();
    }

    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<?> updateCategory(@PathVariable Long categoryId,
                                            @Valid @RequestBody Category categoryRequest) {
        log.info("Requested to update category with id {}", categoryId);
        Category updatedCategory = categoryService.updateCategory(categoryId, categoryRequest);
        return ResponseEntity.status(HttpStatus.OK).body(updatedCategory);
    }
}
