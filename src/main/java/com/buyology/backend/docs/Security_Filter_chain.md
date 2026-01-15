# 🔐 Spring Security: Authentication vs Authorization
## Execution Order Explained (Beginner-Friendly)

> **Important concept:**  
> Spring Security does **NOT** execute security logic in the order you write code.  
> The order in `HttpSecurity` is **configuration only**, not runtime execution.

---

## 🔁 What happens when a request comes?

Every HTTP request follows this **fixed internal flow**:

```
Request → Authentication → Authorization → Controller
```

✅ **Spring Security always authenticates first, then authorizes.**

---

## 🔹 Authentication vs Authorization (VERY IMPORTANT)

### 🔐 Authentication – *Who are you?*

- Happens **first**
- JWT is validated here
- If token is valid → user identity is established
- Result is stored in `SecurityContext`

### 🚦 Authorization – *Are you allowed?*

- Happens **after authentication**
- Uses rules like:

```java
.anyRequest().authenticated()
```

- Checks if authentication exists
- Allows or blocks the request

---

## 🔹 Understanding This Configuration

```java
return httpSecurity
    .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    )
    .formLogin(form -> form.disable())
    .httpBasic(basic -> basic.disable())
    .csrf(csrf -> csrf.disable())

    // Authorization rule
    .authorizeHttpRequests(auth ->
        auth.anyRequest().authenticated()
    )

    // Authentication mechanism (JWT)
    .oauth2ResourceServer(oauth ->
        oauth.jwt(withDefaults())
    )

    .build();
```

---

## ❓ Common Confusion

> **“Authorization is written before `oauth2ResourceServer`.  
> Does authorization run before authentication?”**

### ✅ Correct Answer

**NO.**

Spring Security:

- Uses this code only to **configure rules**
- Internally builds a **Security Filter Chain**
- Executes filters in a **fixed, correct order**

---

## 🔹 Actual Runtime Execution Order

Even though the code is written like this:

```java
.authorizeHttpRequests(...)
.oauth2ResourceServer(...)
```

Spring Security **executes**:

### 1️⃣ JWT Authentication Filter

- Reads `Authorization: Bearer <token>`
- Validates JWT
- Creates `Authentication` object
- Stores it in `SecurityContext`

### 2️⃣ Authorization Filter

- Checks:

```java
.anyRequest().authenticated()
```

- Looks into `SecurityContext`
- Allows or blocks the request

---

## 🔹 What does `.anyRequest().authenticated()` mean?

### Plain English

> “Every request must have a valid authenticated user.”

### Technical Check

Spring internally evaluates:

```java
SecurityContextHolder.getContext().getAuthentication()
```

- If authentication exists → ✅ request allowed
- If not → ❌ **401 Unauthorized**

---

## 🔹 Why this design is good

- Authentication logic is **separate**
- Authorization rules are **clean and readable**
- JWT is **stateless**
- Every request is validated independently
- Secure and scalable

---

## 🧠 Key Takeaway (MEMORIZE THIS)

> **HttpSecurity configuration order does NOT define execution order.**  
> **Spring Security always performs Authentication first and Authorization second.**
