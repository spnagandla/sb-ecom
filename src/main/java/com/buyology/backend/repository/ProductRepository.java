package com.buyology.backend.repository;

import com.buyology.backend.model.Category;
import com.buyology.backend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

    Page<Product> findByCategory(Category category, Pageable pageRequired);

    List<Product> findByQuantityLessThanEqual(Integer quantity);


}


