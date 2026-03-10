# Concurrency / Race Condition Scenarios in this Repository

## Where race conditions can happen today

1. **Concurrent cart updates for the same user (`addProductToCart`)**
   - Two requests can read the same cart total and same `CartItem` quantity, then both write back.
   - Result: lost update on `cart.totalPrice` and/or `cart_item.quantity`.

2. **Concurrent create-cart for the same user (`getOrCreateCart...`)**
   - Two first-time requests can both see "no cart", both try to insert.
   - DB unique constraint prevents duplicates, but one request fails late.

3. **Concurrent product stock checks during cart operations**
   - Multiple users can read the same `product.quantity` before any write (typical check-then-act race).
   - Result: oversubscription risk when stock is low.

4. **Concurrent admin/seller product updates (`updateProduct`)**
   - Two sellers load same product, edit different fields, last write wins.
   - Result: silent lost update.

5. **Concurrent cart quantity patches (`updateProductQuantityInCart`)**
   - Repeated +1/-1 from multiple tabs/devices can interleave and produce inconsistent totals.

## Locking strategy implemented in this branch

### 1) Pessimistic locking (for short critical sections on cart + product reads)

- Added repository methods with `PESSIMISTIC_WRITE`:
  - `CartRepository.findCartByEmailForUpdate(...)`
  - `ProductRepository.findByIdForUpdate(...)`
- `CartServiceImpl.addProductToCart(...)` now:
  - Locks cart row for the current user while computing cart totals/quantities.
  - Locks product row while validating availability.

This reduces interleaving writes for the same cart/product during add-to-cart.

### 2) Optimistic locking (for product updates)

- Added `@Version Long version` on `Product`.
- Added Flyway migration to create `product.version`.
- Added `version` to `ProductDTO` so clients send the version they edited.
- `ProductServiceImpl.updateProduct(...)` now checks version and persists with flush.
- Added global exception handlers returning HTTP `409 CONFLICT` for lock conflicts.

This prevents silent lost updates when two users edit the same product concurrently.

## How to exercise both lock types manually

1. Fetch product A (version = 3) from two clients.
2. Client-1 updates and succeeds (version becomes 4).
3. Client-2 submits old version=3 -> receives **409 conflict** (optimistic lock).
4. Fire two add-to-cart requests for same user/product at same time.
5. One transaction waits for row lock; updates serialize (pessimistic lock behavior).
