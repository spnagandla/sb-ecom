package com.buyology.backend.service;

import com.buyology.backend.dto.CartDTO;
import com.buyology.backend.dto.ProductDTO;
import com.buyology.backend.dto.response.ProductResponseDTO;
import com.buyology.backend.exception.BadRequestException;
import com.buyology.backend.exception.InternalServerException;
import com.buyology.backend.exception.ResourceNotFoundException;
import com.buyology.backend.model.*;
import com.buyology.backend.pagination.PaginationUtil;
import com.buyology.backend.repository.CartRepository;
import com.buyology.backend.repository.CategoryRepository;
import com.buyology.backend.repository.ProductRepository;
import com.buyology.backend.utils.AuthUtil;
import com.buyology.backend.utils.SupabaseStorageClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.dao.OptimisticLockingFailureException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.buyology.backend.utils.CommonMethods.*;

@Service
public class ProductServiceImpl implements ProductService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final CartRepository cartRepository;
    private final SupabaseStorageClient supabaseStorageClient;
    private final CartService cartService;
    private final AuthUtil authUtil;
    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    public ProductServiceImpl(CategoryRepository categoryRepository,
                              ProductRepository productRepository,
                              ModelMapper modelMapper,
                              SupabaseStorageClient supabaseStorageClient,
                              AuthUtil authUtil,CartRepository cartRepository,CartService cartService) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
        this.supabaseStorageClient = supabaseStorageClient;
        this.authUtil = authUtil;
        this.cartRepository = cartRepository;
        this.cartService = cartService;
    }

    @Override
    @Transactional
    public ProductDTO createProduct(Long categoryId, ProductDTO productDTO){
        log.info("Request to save the product @SERVICE");

        User currentUser = authUtil.getAuthenticatedUser();
        boolean allowed = currentUser.getRoles()
                .stream().map(Role::getRoleName)
                .anyMatch(userRoles -> userRoles == UserRoles.ROLE_SELLER ||
                        userRoles == UserRoles.ROLE_ADMIN);

        if(!allowed){
            throw new AccessDeniedException("Only SELLER or ADMIN can add products");
        }
        Product product = modelMapper.map(productDTO, Product.class);
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("category", "categoryId", categoryId));

        product.setCategory(category);

        product.setUser(currentUser);

        BigDecimal price = product.getPrice();
        if (price == null) {
            throw new BadRequestException("Price is required");
        }

        BigDecimal discount = product.getDiscount() == null ? BigDecimal.ZERO : product.getDiscount();

        if (discount.compareTo(BigDecimal.ZERO) < 0 || discount.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BadRequestException("Discount must be between 0 and 100");
        }

        BigDecimal specialPrice = getSpecialPrice(price, discount);

        product.setSpecialPrice(specialPrice);

        Product savedProduct = productRepository.save(product);
        log.info("Successfully saved the product");

        ProductDTO responseDTO = modelMapper.map(savedProduct, ProductDTO.class);
        responseDTO.setImagePath(convertToPublicUrl(savedProduct.getImagePath()));
        return responseDTO;
    }

    @Override
    public ProductResponseDTO getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String orderBy) {
        log.info("Request for List of products @SERVICE");

        Sort sortByAndOrder = sortByAndOrderBy(sortBy, orderBy);
        Pageable pageRequired = getPageRequired(pageNumber, pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findAll(pageRequired);

        List<Product> products = productPage.getContent();
        List<ProductDTO> productDto = products.stream()
                .map(product -> {
                    ProductDTO dto = modelMapper.map(product, ProductDTO.class);
                    dto.setImagePath(convertToPublicUrl(product.getImagePath()));
                    return dto;
                })
                .toList();

        return PaginationUtil.build(productPage, productDto, new ProductResponseDTO());
    }

    @Override
    public ProductResponseDTO searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String orderBy) {
        log.info("Request for List of products related to a category @SERVICE");

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("category", "categoryId", categoryId));

        Sort sortByAndOrder = sortByAndOrderBy(sortBy, orderBy);
        Pageable pageRequired = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findByCategory(category, pageRequired);
        List<Product> products = productPage.getContent();

        List<ProductDTO> productDTO = products.stream()
                .map(product -> {
                    ProductDTO dto = modelMapper.map(product, ProductDTO.class);
                    dto.setImagePath(convertToPublicUrl(product.getImagePath()));
                    return dto;
                })
                .toList();

        return PaginationUtil.build(productPage, productDTO, new ProductResponseDTO());
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        Product product = modelMapper.map(productDTO, Product.class);

        log.info("Request To update the product @SERVICE");
        Product exsistingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("product", "productId", productId));

        if (productDTO.getVersion() == null) {
            throw new BadRequestException("Product version is required for optimistic locking");
        }

        if (!productDTO.getVersion().equals(exsistingProduct.getVersion())) {
            throw new OptimisticLockingFailureException("Product was modified by another transaction");
        }

        exsistingProduct.setProductName(product.getProductName());
        exsistingProduct.setDescription(product.getDescription());
        exsistingProduct.setQuantity(product.getQuantity());
        exsistingProduct.setPrice(product.getPrice());
        exsistingProduct.setDiscount(product.getDiscount());
        exsistingProduct.setSpecialPrice(getSpecialPrice(product.getPrice(), product.getDiscount()));

        Product savedProduct = productRepository.saveAndFlush(exsistingProduct);

        List<Cart> carts = cartRepository.findCartByProductId(productId);

        List<CartDTO> cartDTOs = carts.stream().map(cart ->{
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
            List<ProductDTO> products = cart.getCartItems().stream()
                    .map( p -> modelMapper.map(p.getProduct(),ProductDTO.class))
                    .collect(Collectors.toList());
            cartDTO.setProducts(products);
            return cartDTO;
        }).collect(Collectors.toList());

        cartDTOs.forEach(cart -> cartService.updateProductInCarts(cart.getCartID(),productId));

        log.info("Saved the Updated Product To DB @SERVICE with optimistic version={}", savedProduct.getVersion());
        ProductDTO responseDTO = modelMapper.map(savedProduct, ProductDTO.class);
        responseDTO.setImagePath(convertToPublicUrl(savedProduct.getImagePath()));
        return responseDTO;
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        List<Cart> carts = cartRepository.findCartByProductId(productId);
        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(), productId));

        productRepository.deleteById(productId);
        log.info("Product With ID:{} Deleted Successfully @SERVICE", productId);
        ProductDTO responseDTO = modelMapper.map(existingProduct, ProductDTO.class);
        responseDTO.setImagePath(convertToPublicUrl(existingProduct.getImagePath()));
        return responseDTO;
    }

    @Override
    @Transactional
    public ProductDTO updateProductImage(Long productId, MultipartFile image) {

        log.info("Request to update image for productId={}", productId);
        validateImage(image);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        try {
            String ext = extensionFromContentType(image.getContentType());
            String objectPath = "products/" + productId + "/" + UUID.randomUUID() + ext;

            log.info("Generated storage path for productId={}: {}", productId, objectPath);

            byte[] bytes;
            try {
                bytes = image.getBytes();
            } catch (IOException e) {
                // Reading the uploaded file failed on server side -> 500
                throw new InternalServerException("Failed to read uploaded image",e);
            }

            supabaseStorageClient.upload(objectPath, bytes, image.getContentType());
            log.info("Image upload successful for productId={}", productId);

            product.setImagePath(objectPath);
            Product saved = productRepository.save(product);
            log.info("Product imagePath updated in DB for productId={}", productId);

            ProductDTO responseDTO = modelMapper.map(saved, ProductDTO.class);
            responseDTO.setImagePath(convertToPublicUrl(saved.getImagePath()));
            return responseDTO;

        } catch (Exception e) {
            // Server-side failure → 500
            throw new InternalServerException("Failed to upload image. Please try again later.",e);
        }
    }

    private String convertToPublicUrl(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }
        if (imagePath.startsWith("http")) {
            return imagePath;
        }
        return supabaseStorageClient.publicUrl(imagePath);
    }

    private static BigDecimal getSpecialPrice(BigDecimal price, BigDecimal discount) {
        return price.subtract(
                        price.multiply(discount)
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                ).max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
