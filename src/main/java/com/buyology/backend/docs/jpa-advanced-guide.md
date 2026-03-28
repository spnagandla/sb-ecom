# 🧠 JPA @Transactional & Dirty Checking — Advanced Guide

---

## 🚀 Core Idea

When you use `@Transactional`:

- Hibernate opens a **Persistence Context (Session)**
- Entities fetched inside become **managed**
- Hibernate tracks changes automatically (**Dirty Checking**)
- At transaction end → SQL is generated automatically

---

## 📊 Visual Flow

```
[Database]
     ↓ fetch
[Persistence Context (Session)]
     ↓ modify
[Dirty Checking]
     ↓ flush
[SQL Generated]
     ↓ commit
[Database Updated]
```

---

## 🧾 Dirty Checking Explained

Hibernate stores a **snapshot** of entity state:

| Field | Original | Current |
|------|--------|--------|
| totalPrice | 300 | 500 |

👉 Difference detected → UPDATE query generated

---

## 🔄 Persistence Context (VERY IMPORTANT)

Think of it as:

👉 A **map of all managed entities**

```
Persistence Context:
---------------------
Cart#1 → Managed
CartItem#1 → Managed
CartItem#2 → Managed
```

👉 Hibernate tracks ALL of them

---

## 🔥 Example

```java
@Transactional
public void updateCart(Long cartId) {

    Cart cart = cartRepository.findById(cartId).get();

    cart.setTotalPrice(new BigDecimal("500"));
}
```

👉 Generated SQL:

```
UPDATE cart SET total_price = 500 WHERE cart_id = ?
```

---

## ⚠️ Flush vs Commit

### 🔹 Flush
- Syncs changes to DB
- Does NOT end transaction

### 🔹 Commit
- Finalizes transaction
- Makes changes permanent

---

### Example

```java
entityManager.flush();
```

👉 Forces SQL execution early

---

## 🔥 Real Scenario (Cart System)

```java
@Transactional
public void deleteProductFromCart(Long cartId, Long productId) {

    Cart cart = cartRepository.findById(cartId).get();
    CartItem item = cartItemRepository.find(...);

    cart.setTotalPrice(...);
    cartItemRepository.delete(item);
}
```

---

### What Hibernate does:

```
DELETE FROM cart_item WHERE ...
UPDATE cart SET total_price = ...
```

👉 Order may vary depending on flush timing

---

## ⚠️ Common Pitfalls

### ❌ 1. No @Transactional

👉 Changes NOT saved

---

### ❌ 2. Detached Entities

👉 Hibernate does NOT track

---

### ❌ 3. Large Loops

```java
for (item : items) {
    item.setQuantity(...);
}
```

👉 Generates N queries → performance issue

---

## 🚀 Optimization

### Batch Updates

```properties
spring.jpa.properties.hibernate.jdbc.batch_size=50
```

---

## 🧠 Concurrency Insight (IMPORTANT)

If 2 users update same cart:

```
Thread 1 → reads 100
Thread 2 → reads 100

Thread 1 → writes 200
Thread 2 → writes 150 ❌ lost update
```

👉 Use:
- Optimistic Locking (`@Version`)
- Pessimistic Locking

---

## 🔥 Mental Model

👉 `@Transactional` = recording session 🎥

- Fetch → tracked
- Modify → detected
- End → persisted

---

## 💥 Interview Answer

> JPA uses a persistence context to manage entities within a transaction. Hibernate performs dirty checking by comparing snapshots and automatically synchronizes changes with the database at flush/commit time.

---

## 🧠 Final Takeaways

- No need for `save()` for managed entities
- Use `@Transactional` always for updates
- Understand persistence context deeply
- Be careful with concurrency and performance

---
