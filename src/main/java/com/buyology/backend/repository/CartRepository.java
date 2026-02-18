package com.buyology.backend.repository;

import com.buyology.backend.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("SELECT c FROM Cart c WHERE c.user.email = ?1")
    Cart findCartByEmail(String email);
}


//Why I am writing the custom query because the cart has the user and user has the email.
// so the Jpa will not automatically generate the query for the nested objects so.
//?1 is the 1st parameter to be passed (Positional parameters)
