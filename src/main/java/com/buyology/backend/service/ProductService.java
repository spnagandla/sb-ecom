package com.buyology.backend.service;

import com.buyology.backend.dto.ProductDTO;
import com.buyology.backend.dto.response.ProductResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.AccessDeniedException;

@Service
public interface ProductService {
    ProductDTO createProduct(Long categoryId,ProductDTO productDTO);
    ProductResponseDTO getAllProducts(Integer pageNumber,Integer pageSize,String sortBy,String orderBy);
    ProductResponseDTO searchByCategory(Long categoryId,Integer pageNumber,Integer pageSize,String sortBy,String orderBy);
    ProductDTO updateProduct(Long productId,ProductDTO productDTO);
    ProductDTO deleteProduct(Long productId);
    ProductDTO updateProductImage(Long productId, MultipartFile image);
}
