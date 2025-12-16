package com.buyology.backend.service;

import com.buyology.backend.dto.ProductDTO;
import com.buyology.backend.exception.ResourceNotFoundException;
import com.buyology.backend.model.Category;
import com.buyology.backend.model.Product;
import com.buyology.backend.repository.CategoryRepository;
import com.buyology.backend.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ProductServiceImpl implements ProductService{

    private final CategoryRepository categoryRepository;
    private final ProductRepository  productRepository;
    private final ModelMapper modelMapper;
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    public ProductServiceImpl(CategoryRepository categoryRepository,
                              ProductRepository productRepository,
                              ModelMapper modelMapper){
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public ProductDTO createProduct(Long categoryId, Product product){
        log.info("Request to save the product @SERVICE");
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("category","categoryId",categoryId));

        product.setCategory(category);
        product.setImage("default.png");
        BigDecimal specialPrice = product.getPrice()
                .subtract(
                        product.getPrice()
                                .multiply(product.getDiscount())
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                );
        product.setSpecialPrice(specialPrice);
        Product savedProduct = productRepository.save(product);
        log.info("Successfully saved the product");
        return modelMapper.map(savedProduct, ProductDTO.class);
    }
}
