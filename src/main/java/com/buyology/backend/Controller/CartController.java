package com.buyology.backend.Controller;

import com.buyology.backend.dto.CartDTO;
import com.buyology.backend.model.Cart;
import com.buyology.backend.service.CartService;
import com.buyology.backend.utils.AuthUtil;
import jakarta.persistence.GeneratedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CartController {

    private final CartService cartService;
    private final AuthUtil authUtil;
    private static final Logger log = LoggerFactory.getLogger(CartController.class);


    public CartController(CartService cartService, AuthUtil authUtil){
        this.cartService = cartService;
        this.authUtil = authUtil;
    }

    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long productId,
                                                    @PathVariable Integer quantity){
        log.info("Requested to add product to cart");
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addProductToCart(productId,quantity));
    }

    @GetMapping("/carts")
    public ResponseEntity<List<CartDTO>> getCarts(){
        List<CartDTO> cartDTOs = cartService.getAllCarts();
        return ResponseEntity.ok(cartDTOs);
    }


    @GetMapping("/carts/me")
    public ResponseEntity<CartDTO> getUserCart() {
        String email = authUtil.loggedInEmail();
        return ResponseEntity.ok(cartService.getUserCart(email));
    }

    @PatchMapping("/cart/products/{productId}/quantity/{operation}")
    public ResponseEntity<CartDTO>updateCartProductQuantity(@PathVariable Long productId,
                                                            @PathVariable String operation){
        return ResponseEntity.ok(cartService.updateProductQuantityInCart(productId,operation.equalsIgnoreCase("delete") ? -1 : 1));
    }

}
