package com.buyology.backend.Controller;

import com.buyology.backend.config.AppConstants;
import com.buyology.backend.dto.CategoryDTO;
import com.buyology.backend.dto.ProductDTO;
import com.buyology.backend.dto.response.ProductResponseDTO;
import com.buyology.backend.model.Product;
import com.buyology.backend.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    public ProductController(ProductService productService){ this.productService = productService;}

    @PostMapping("/admin/categories/{categoryId}/product")
    public ResponseEntity<ProductDTO> createProduct(
            @Valid @RequestBody ProductDTO productDTO,
            @PathVariable Long categoryId){
        log.info("Requested to add new product @CONTROLLER");
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(categoryId,productDTO));
    }

    @GetMapping("/public/products")
    public ResponseEntity<ProductResponseDTO> getAllProducts(
            @RequestParam(name = "page", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "size", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false)String sortBy,
            @RequestParam(name = "orderBy",defaultValue = AppConstants.SORT_DIRECTION, required = false)String orderBy
    ){
        log.info("Requesting for all the products @CONTROLLER");
        return ResponseEntity.ok(productService.getAllProducts(pageNumber,pageSize, sortBy,orderBy));
    }

    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponseDTO> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(name = "page", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "size", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY, required = false)String sortBy,
            @RequestParam(name = "orderBy",defaultValue = AppConstants.SORT_DIRECTION, required = false)String orderBy
    ){
        log.info("Requesting for all the products related to a category @CONTROLLER");
        return ResponseEntity.ok(productService.searchByCategory(categoryId,pageNumber,pageSize, sortBy,orderBy));
    }

    @PutMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(
            @Valid @RequestBody ProductDTO productDTO,
            @PathVariable Long productId){

        log.info("Request To Update Product @CONTROLLER");
        return ResponseEntity.status(HttpStatus.OK).body(productService.updateProduct(productId, productDTO));
    }

    @DeleteMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable Long productId){
        log.info("Requested to Delete Product with id:{}", productId);
        return ResponseEntity.ok(productService.deleteProduct(productId));
    }

    @PatchMapping(
            value = "/products/{productId}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )

    public ResponseEntity<ProductDTO> updateProductImage(
            @PathVariable Long productId,
            @RequestParam(name ="image")MultipartFile image
            ){
        log.info("Requested to update Product image with id:{}", productId);
        return ResponseEntity.ok(productService.updateProductImage(productId,image));
    }

}
