package com.buyology.backend.repository;

import com.buyology.backend.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> {
    Category findByCategoryName(
            @NotBlank(message =" Category name can't be null")
            @Size(min = 5,message = "Category must contain at least 5 characters")
            String categoryName);
}
