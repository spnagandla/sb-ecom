package com.buyology.backend.Controller;

import com.buyology.backend.dto.CategoryDTO;
import com.buyology.backend.dto.response.CategoryResponseDTO;
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
    public ResponseEntity<CategoryResponseDTO> getAllCategories(
            @RequestParam(name = "page") Integer pageNumber,
            @RequestParam(name = "size") Integer pageSize){

        log.info("Requesting for all the categories");
        com.buyology.backend.dto.response.CategoryResponseDTO categoryResponse = categoryService.getAllCategories(pageNumber,pageSize);
        return ResponseEntity.ok(categoryResponse);
    }

    @PostMapping("/public/categories")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryDTO categoryDTO){
        log.info("Requested to add a new Category");
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(categoryDTO));
    }

    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId){
        log.info("requested to delete category with id:{}", categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.deleteCategory(categoryId));
    }

    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<?> updateCategory(@PathVariable Long categoryId,
                                            @Valid @RequestBody CategoryDTO categoryRequest) {
        log.info("Requested to update category with id {}", categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(categoryService.updateCategory(categoryId, categoryRequest));
    }

    //Don't use this unless if there is a strong reason
    @DeleteMapping("/admin/categories")
    public ResponseEntity<Void> deleteCategory(){
        log.info("Requested to empty database");
        categoryService.deleteAllCategory();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/categories/bulk")
    public ResponseEntity<List<CategoryDTO>> createCategories(@Valid @RequestBody List<CategoryDTO> categoryDTO){
        log.info("Requested to add a bunch of categories");
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createBulkCategories(categoryDTO));
    }

}
