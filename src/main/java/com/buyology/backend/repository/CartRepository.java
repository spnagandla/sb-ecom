package com.buyology.backend.repository;

import com.buyology.backend.model.Cart;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("SELECT c FROM Cart c WHERE c.user.email = ?1")
    Cart findCartByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cart c WHERE c.user.email = :email")
    Cart findCartByEmailForUpdate(@Param("email") String email);

    @Query("SELECT c FROM Cart c JOIN FETCH c.cartItems ci JOIN FETCH ci.product p WHERE p.productId = ?1")
    List<Cart> findCartByProductId(Long productId);
}


//Why I am writing the custom query because the cart has the user and user has the email.
// so the Jpa will not automatically generate the query for the nested objects so.
//?1 is the 1st parameter to be passed (Positional parameters)
