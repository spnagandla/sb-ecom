package com.buyology.backend.Controller;

import com.buyology.backend.dto.CartDTO;
import com.buyology.backend.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CartController {

    private final CartService cartService;
    private static final Logger log = LoggerFactory.getLogger(CartController.class);


    public CartController(CartService cartService){
        this.cartService = cartService;
    }

    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long productId,
                                                    @PathVariable Integer quantity){
        log.info("Requested to add product to cart");
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addProductToCart(productId,quantity));
    }


}
