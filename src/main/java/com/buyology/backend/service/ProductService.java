package com.buyology.backend.service;

import com.buyology.backend.dto.ProductDTO;
import com.buyology.backend.dto.response.ProductResponseDTO;
import com.buyology.backend.model.Product;
import org.springframework.stereotype.Service;

@Service
public interface ProductService {
    ProductDTO createProduct(Long categoryId,Product product);
    ProductResponseDTO getAllProducts(Integer pageNumber,Integer pageSize,String sortBy,String orderBy);
}
