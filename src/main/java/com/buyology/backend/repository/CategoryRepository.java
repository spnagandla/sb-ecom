package com.buyology.backend.repository;

import com.buyology.backend.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {
    Category findByCategoryName(
            @NotBlank(message ="Category name can't be null")
            @Size(min = 3,message = "Category must contain at least 3 characters")
            String categoryName);

    List<Category> findByCategoryNameIn(List<String> categoryNames);
}
