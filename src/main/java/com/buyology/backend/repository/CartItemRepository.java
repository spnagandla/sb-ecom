package com.buyology.backend.repository;

import com.buyology.backend.model.CartItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query(" SELECT ci FROM CartItem ci WHERE ci.cart.cartId = ?1 AND ci.product.productId = ?2")
    CartItem findCartItemByProductIdAndCartId(Long cartId, Long productId);



    @Query("""
            select ci from CartItem ci
            join fetch ci.cart c
            join fetch ci.product p
            join c.user u
            where u.email = ?1 and p.productId = ?2
            """)
    Optional<CartItem> findCartItemForUserAndProduct(String email, Long productId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select ci from CartItem ci
            where ci.cart.cartId = :cartId and ci.product.productId = :productId
            """)
    Optional<CartItem> findByCartAndProductForUpdate(@Param("cartId") Long cartId,
                                                     @Param("productId") Long productId);

}



/*
===============================================================================
JPQL JOIN vs JOIN FETCH – Theory Notes
===============================================================================

1) What is JOIN in JPQL?

JOIN is used to navigate relationships between entities.

Example:
    join ci.cart c

This means:
    - Start from CartItem (ci)
    - Follow the "cart" relationship
    - Alias it as "c"

JOIN does NOT automatically load the joined entity into memory.
It is primarily used for:
    - Filtering
    - Applying conditions (WHERE clause)
    - Writing relational queries

JOIN affects the SQL that is generated.
It does NOT change how Hibernate loads objects into memory.

-------------------------------------------------------------------------------

2) What is FETCH in JPQL?

FETCH is used together with JOIN:

    join fetch ci.cart

JOIN FETCH tells Hibernate:
    - Load this related entity immediately
    - Avoid lazy loading later
    - Retrieve it in the same SQL query

FETCH affects object loading (hydration).
It changes how Hibernate populates the entity graph.

-------------------------------------------------------------------------------

3) Why JOIN FETCH is needed?

By default:
    - @OneToMany is LAZY
    - @ManyToOne is often LAZY

LAZY means:
    The related entity is NOT loaded immediately.
    It will trigger an additional SQL query when accessed.

Example without FETCH:
    1 query → load CartItem
    1 query → load Cart
    1 query → load Product

Example with JOIN FETCH:
    1 query → load CartItem + Cart + Product

This prevents the N+1 query problem.

-------------------------------------------------------------------------------

4) When to use JOIN only?

Use JOIN (without FETCH) when:
    - You only need the related entity for filtering.
    - You do not need it in memory.
    - Example: filtering by user.email

Example:
    join c.user u
    where u.email = :email

Here, User is only used to filter rows.
We do not need the User object loaded in memory.
So FETCH is unnecessary.

-------------------------------------------------------------------------------

5) Rule of Thumb

If you need the related object in Java code:
    → use JOIN FETCH

If you only need it in WHERE clause:
    → use JOIN

-------------------------------------------------------------------------------

6) Important Warning

Avoid multiple JOIN FETCH on large collections,
because it can:
    - Multiply result rows
    - Cause Cartesian product issues
    - Hurt performance

Use FETCH carefully and intentionally.

===============================================================================
*/