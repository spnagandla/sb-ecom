package com.buyology.backend.service;

import com.buyology.backend.dto.CartDTO;

public interface CartService {
    CartDTO addProductToCart(Long productId, Integer quantity);
}
