package com.buyology.backend.service;

import com.buyology.backend.exception.ResourceNotFoundException;
import com.buyology.backend.model.Cart;
import com.buyology.backend.model.CartItem;
import com.buyology.backend.model.Product;
import com.buyology.backend.repository.CartItemRepository;
import com.buyology.backend.repository.CartRepository;
import com.buyology.backend.repository.ProductRepository;
import com.buyology.backend.utils.AuthUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void deleteProductFromCart_usesCartIdAndProductIdInCorrectOrder() {
        Long cartId = 3L;
        Long productId = 232L;

        Cart cart = new Cart();
        cart.setCartId(cartId);
        cart.setTotalPrice(new BigDecimal("399.99"));

        Product product = new Product();
        product.setProductId(productId);
        product.setProductName("Desk Lamp");

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(1);
        cartItem.setProductPrice(new BigDecimal("399.99"));

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findCartItemByCartIdAndProductId(cartId, productId)).thenReturn(cartItem);

        String result = cartService.deleteProductFromCart(cartId, productId);

        assertEquals("Product Desk Lamp removed from the cart", result);
        verify(cartItemRepository).findCartItemByCartIdAndProductId(cartId, productId);
        verify(cartItemRepository).deleteCartItemByCartIdAndProductId(cartId, productId);
    }

    @Test
    void deleteProductFromCart_throwsWhenCartIsMissing() {
        Long cartId = 3L;
        Long productId = 232L;

        when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.deleteProductFromCart(cartId, productId));

        verify(cartItemRepository, never()).findCartItemByCartIdAndProductId(cartId, productId);
        verify(cartItemRepository, never()).deleteCartItemByCartIdAndProductId(cartId, productId);
    }
}
