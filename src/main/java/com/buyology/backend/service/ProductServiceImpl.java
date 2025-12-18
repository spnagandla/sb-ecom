package com.buyology.backend.service;

import com.buyology.backend.dto.ProductDTO;
import com.buyology.backend.dto.response.ProductResponseDTO;
import com.buyology.backend.exception.APIException;
import com.buyology.backend.exception.ResourceNotFoundException;
import com.buyology.backend.model.Category;
import com.buyology.backend.model.Product;
import com.buyology.backend.repository.CategoryRepository;
import com.buyology.backend.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.buyology.backend.utils.CommonMethods.getPageRequired;
import static com.buyology.backend.utils.CommonMethods.sortByAndOrderBy;

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
    @Transactional
    public ProductDTO createProduct(Long categoryId, Product product) {
        log.info("Request to save the product @SERVICE");
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("category", "categoryId", categoryId));

        product.setCategory(category);
        product.setImage("default.png");

        BigDecimal price = product.getPrice();
        if (price == null) {
            throw new IllegalArgumentException("Price is required");
        }

        BigDecimal discount = product.getDiscount() == null ? BigDecimal.ZERO : product.getDiscount();

        if (discount.compareTo(BigDecimal.ZERO) < 0 || discount.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Discount must be between 0 and 100");
        }

        BigDecimal specialPrice = price.subtract(
                        price.multiply(discount)
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                ).max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        product.setSpecialPrice(specialPrice);

        Product savedProduct = productRepository.save(product);
        log.info("Successfully saved the product");

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductResponseDTO getAllProducts(Integer pageNumber,Integer pageSize,String sortBy,String orderBy){
        log.info("Request for List of products @SERVICE");

        Sort sortByAndOrder = sortByAndOrderBy(sortBy,orderBy);
        Pageable pageRequired = getPageRequired(pageNumber,pageSize,sortByAndOrder);
        Page<Product> productPage = productRepository.findAll(pageRequired);

        List<Product> products = productPage.getContent();
        if(products.isEmpty()) throw new APIException("No Products Found");

        List<ProductDTO> ProductDto = products.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();

        ProductResponseDTO productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setContent(ProductDto);
        productResponseDTO.setPageNumber(productPage.getNumber());
        productResponseDTO.setPageSize(productPage.getSize());
        productResponseDTO.setTotalElements(productPage.getTotalElements());
        productResponseDTO.setTotalPages(productPage.getTotalPages());
        productResponseDTO.setLastPage(productPage.isLast());
        return productResponseDTO;
    }
}
