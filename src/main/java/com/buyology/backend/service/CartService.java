package com.buyology.backend.service;

import com.buyology.backend.dto.CartDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CartService {
    CartDTO addProductToCart(Long productId, Integer quantity);

    List<CartDTO> getAllCarts();

    CartDTO getUserCart(String email);

    @Transactional
    CartDTO updateProductQuantityInCart(Long productId, Integer delta);

    String deleteProductFromCart(Long cartId, Long productId);

    void updateProductInCarts(Long cartId, Long productId);

}
