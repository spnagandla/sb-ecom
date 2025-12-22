package com.buyology.backend.service;

import com.buyology.backend.dto.ProductDTO;
import com.buyology.backend.dto.response.ProductResponseDTO;
import com.buyology.backend.exception.ResourceNotFoundException;
import com.buyology.backend.model.Category;
import com.buyology.backend.model.Product;
import com.buyology.backend.pagination.PaginationUtil;
import com.buyology.backend.repository.CategoryRepository;
import com.buyology.backend.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public ProductDTO createProduct(Long categoryId, ProductDTO productDTO) {
        log.info("Request to save the product @SERVICE");
        Product product = modelMapper.map(productDTO,Product.class);
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

        BigDecimal specialPrice = getSpecialPrice(price,discount);

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
        List<ProductDTO> productDto = products.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();

        return PaginationUtil.build(productPage,productDto,new ProductResponseDTO());
    }

    @Override
    public ProductResponseDTO searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String orderBy) {
        log.info("Request for List of products related to a category @SERVICE");

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("category", "categoryId", categoryId));

        Sort sortByAndOrder =sortByAndOrderBy(sortBy,orderBy);
        Pageable pageRequired = PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> productPage = productRepository.findByCategory(category, pageRequired);
        List<Product> products = productPage.getContent();

        List<ProductDTO> productDTO = products.stream()
                .map(product -> modelMapper.map(product,ProductDTO.class))
                .toList();

        return PaginationUtil.build(productPage,productDTO,new ProductResponseDTO());
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        Product product = modelMapper.map(productDTO,Product.class);

        log.info("Request To update the product @SERVICE");
        Product exsistingProduct = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("product","productId",productId));

        exsistingProduct.setProductName(product.getProductName());
        exsistingProduct.setDescription(product.getDescription());
        exsistingProduct.setQuantity(product.getQuantity());
        exsistingProduct.setPrice(product.getPrice());
        exsistingProduct.setDiscount(product.getDiscount());
        exsistingProduct.setSpecialPrice(getSpecialPrice(product.getPrice(),product.getDiscount()));

        Product savedProduct = productRepository.save(exsistingProduct);
        log.info("Saved the Updated Product To DB @SERVICE");
        return modelMapper.map(savedProduct,ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","productId",productId));
        productRepository.deleteById(productId);
        log.info("Product With ID:{} Deleted Successfully @SERVICE", productId);
        return modelMapper.map(existingProduct,ProductDTO.class);
    }

    private static BigDecimal getSpecialPrice(BigDecimal price, BigDecimal discount) {
        return price.subtract(
                        price.multiply(discount)
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                ).max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
