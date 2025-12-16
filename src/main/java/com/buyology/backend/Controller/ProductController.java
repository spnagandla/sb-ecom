package com.buyology.backend.Controller;

import com.buyology.backend.dto.ProductDTO;
import com.buyology.backend.model.Product;
import com.buyology.backend.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    public ProductController(ProductService productService){ this.productService = productService;}

    @PostMapping("/admin/categories/{categoryId}/product)")
    public ResponseEntity<ProductDTO> createProduct(
            @Valid @RequestBody Product product,
            @PathVariable Long categoryId){
        log.info("Requested to add new product");
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(categoryId,product));

    }
}
