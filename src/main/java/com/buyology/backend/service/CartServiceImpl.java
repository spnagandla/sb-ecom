package com.buyology.backend.service;

import com.buyology.backend.dto.CartDTO;
import com.buyology.backend.dto.ProductDTO;
import com.buyology.backend.exception.APIException;
import com.buyology.backend.exception.ResourceNotFoundException;
import com.buyology.backend.model.Cart;
import com.buyology.backend.model.CartItem;
import com.buyology.backend.model.Product;
import com.buyology.backend.repository.CartItemRepository;
import com.buyology.backend.repository.CartRepository;
import com.buyology.backend.repository.ProductRepository;
import com.buyology.backend.repository.UserRepository;
import com.buyology.backend.utils.AuthUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);
    private final CartItemRepository cartItemRepository;
    private final ModelMapper modelMapper;
    private final AuthUtil authUtil;


    public CartServiceImpl(ProductRepository productRepository, CartRepository cartRepository, CartItemRepository cartItemRepository, ModelMapper modelMapper, AuthUtil authUtil) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.modelMapper = modelMapper;
        this.authUtil = authUtil;
    }

    @Override
    @Transactional
    public CartDTO addProductToCart(Long productId, Integer quantity) {

        validateQuantity(quantity);

        Cart cart = getOrCreateCartForLoggedInUser();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("product", "productId", productId));

        log.info("Add to cart: email={}, cartId={}, productId={}, requestedQty={}",
                authUtil.loggedInEmail(), cart.getCartId(), productId, quantity);

        // Find item if it already exists in the cart
        CartItem cartItem = cartItemRepository
                .findCartItemByProductIdAndCartId(cart.getCartId(), product.getProductId());

        int currentCartQty = (cartItem == null) ? 0 : cartItem.getQuantity();
        int newCartQty = currentCartQty + quantity;

        // Stock checks against the *new* cart quantity
        ensureProductAvailable(product, newCartQty);

        // Price delta for only the newly added quantity
        BigDecimal delta = product.getSpecialPrice()
                .multiply(BigDecimal.valueOf(quantity));

        if (cartItem == null) {
            cartItem = buildCartItem(cart, product, newCartQty);

            // keep in-memory relationship consistent
            cart.getCartItems().add(cartItem);

            cartItemRepository.save(cartItem);

            log.info("CartItem created: cartId={}, productId={}, qty={}, delta={}",
                    cart.getCartId(), productId, newCartQty, delta);
        } else {
            cartItem.setQuantity(newCartQty);
            cartItemRepository.save(cartItem);

            log.info("CartItem updated: cartId={}, productId={}, oldQty={}, newQty={}, delta={}",
                    cart.getCartId(), productId, currentCartQty, newCartQty, delta);
        }

        // Update cart total (BigDecimal-safe)
        cart.setTotalPrice(cart.getTotalPrice().add(delta));
        cartRepository.save(cart);

        log.info("Cart total updated: cartId={}, newTotal={}", cart.getCartId(), cart.getTotalPrice());

        return toCartDTO(cart);
    }

    @Override
    public List<CartDTO> getAllCarts() {


        List<Cart> carts = cartRepository.findAll();

        if(carts.size() == 0){
            throw new APIException("no carts found");
        }

        return carts.stream()
                .map(cart -> {
                    CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);
                    List<ProductDTO> products = cart.getCartItems().stream()
                            .map( product -> modelMapper.map(product, ProductDTO.class))
                            .collect(Collectors.toList());
                    cartDTO.setProducts(products);
                    return cartDTO;
                }).collect(Collectors.toList());

    }

    @Override
    public CartDTO getUserCart(String email) {
            Cart cart = cartRepository.findCartByEmail(email);
            if(cart == null){
                throw new ResourceNotFoundException("No cart found!");
            }

            CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);

            cart.getCartItems().forEach(c -> c.getProduct().setQuantity(c.getQuantity()));

            List<ProductDTO> products = cart.getCartItems().stream()
                    .map(p -> modelMapper.map(p.getProduct(),ProductDTO.class))
                    .toList();
            cartDTO.setProducts(products);
            return cartDTO;
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer delta) {

        String email = authUtil.loggedInEmail();

        CartItem cartItem = cartItemRepository.findCartItemForUserAndProduct(email, productId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "productId", productId));

        Cart cart = cartItem.getCart();
        Product product = cartItem.getProduct();

        int currentQty = cartItem.getQuantity() == null ? 0 : cartItem.getQuantity();
        int newQty = currentQty + delta;

        // Prevent negative quantities
        if (newQty < 0) {
            throw new APIException("Quantity cannot be negative");
        }

        // Stock validation only when increasing
        Integer stock = product.getQuantity();
        if (delta == 1) {
            if (stock == null || stock <= 0) {
                throw new APIException(product.getProductName() + " is not available");
            }
            if (newQty > stock) {
                throw new APIException("Only " + stock + " available for " + product.getProductName());
            }
        }

        // Price math (BigDecimal)
        BigDecimal unitPrice = product.getSpecialPrice() != null ? product.getSpecialPrice() : BigDecimal.ZERO;
        BigDecimal cartTotal = cart.getTotalPrice() != null ? cart.getTotalPrice() : BigDecimal.ZERO;

        // Update cart total by delta amount
        BigDecimal deltaAmount = unitPrice.multiply(BigDecimal.valueOf(delta));
        cart.setTotalPrice(cartTotal.add(deltaAmount));

        // Update or delete item
        if (newQty == 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(newQty);
            cartItem.setProductPrice(unitPrice);
            cartItem.setDiscount(product.getDiscount() == null ? BigDecimal.ZERO : product.getDiscount());
            // no need to call save explicitly inside @Transactional (ok if you do)
        }

        // Return DTO (use your existing mapper logic)
        return convertToCartDTO(cart);
    }


    private CartDTO convertToCartDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setCartID(cart.getCartId());
        dto.setTotalPrice(cart.getTotalPrice() == null ? 0.0 : cart.getTotalPrice().doubleValue());

        List<ProductDTO> products = cart.getCartItems().stream()
                .map(item -> {
                    ProductDTO p = modelMapper.map(item.getProduct(), ProductDTO.class);
                    p.setQuantity(item.getQuantity()); // quantity in cart comes from CartItem
                    return p;
                })
                .toList();

        dto.setProducts(products);
        return dto;
    }
    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new APIException("Quantity must be greater than 0");
        }
    }

    private Cart getOrCreateCartForLoggedInUser() {
        String email = authUtil.loggedInEmail();
        Cart existing = cartRepository.findCartByEmail(email);
        if (existing != null) return existing;

        Cart cart = new Cart();
        cart.setTotalPrice(BigDecimal.ZERO);
        cart.setUser(authUtil.loggedInUser());

        Cart saved = cartRepository.save(cart);
        log.info("New cart created: email={}, cartId={}", email, saved.getCartId());
        return saved;
    }

    private void ensureProductAvailable(Product product, int requestedQty) {
        int available = product.getQuantity();

        if (available <= 0) {
            throw new APIException(product.getProductName() + " is not available");
        }
        if (available < requestedQty) {
            throw new APIException("Please order " + product.getProductName()
                    + " less than or equal to available quantity " + available + ".");
        }
    }

    private CartItem buildCartItem(Cart cart, Product product, int quantity) {
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);

        item.setDiscount(product.getDiscount());
        item.setProductPrice(product.getSpecialPrice());

        return item;
    }

    private CartDTO toCartDTO(Cart cart) {
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<ProductDTO> products = cart.getCartItems().stream()
                .map(item -> {
                    ProductDTO dto = modelMapper.map(item.getProduct(), ProductDTO.class);
                    dto.setQuantity(item.getQuantity()); // cart quantity
                    return dto;
                })
                .toList();

        cartDTO.setProducts(products);
        return cartDTO;
    }

}
