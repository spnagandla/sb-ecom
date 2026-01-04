# 🔐 JWT Authentication Flow (Spring Security + Supabase)

This document explains **how JWT authentication works end-to-end** in this project using **Spring Security (OAuth2 Resource Server)** and **Supabase** as the Identity Provider.

---

## 🧠 High-Level Architecture

```text
User / Postman / Frontend
        |
        |  Authorization: Bearer <JWT>
        v
Spring Boot Application (Resource Server)
        |
        |-- SecurityFilterChain
        |     |
        |     |-- BearerTokenAuthenticationFilter
        |     |     - Extracts JWT from Authorization header
        |     |     - Creates BearerTokenAuthenticationToken
        |     |     - Calls AuthenticationManager.authenticate()
        |     |
        |     |-- AuthenticationManager
        |     |     - Delegates authentication to JwtAuthenticationProvider
        |     |
        |     |-- JwtAuthenticationProvider
        |     |     - Calls JwtDecoder.decode(token)
        |     |
        |     |-- JwtDecoder
        |     |     - Fetches public keys from Supabase JWKS (cached)
        |     |     - Verifies signature
        |     |     - Validates exp, nbf, iss
        |     |
        |     |-- SecurityContext
        |     |     - Stores authenticated JwtAuthenticationToken
        |     |
        |     |-- Authorization Filter
        |     |     - anyRequest().authenticated()
        |
        v
Controller Method Executes
```

---

## 🔄 Request Lifecycle (Step-by-Step)

### 1️⃣ Client sends request
The client (Postman / frontend) sends a request with a JWT:

```http
Authorization: Bearer eyJhbGciOi...
```

---

### 2️⃣ Request enters Spring Security Filter Chain
Every HTTP request passes through Spring Security before reaching any controller.

---

### 3️⃣ BearerTokenAuthenticationFilter
- Extracts JWT from the `Authorization` header
- Wraps it in a `BearerTokenAuthenticationToken`
- Passes it to the AuthenticationManager

---

### 4️⃣ AuthenticationManager
- Central coordinator for authentication
- Chooses the correct AuthenticationProvider
- For JWTs → delegates to `JwtAuthenticationProvider`

---

### 5️⃣ JwtAuthenticationProvider
- Uses `JwtDecoder`
- Performs cryptographic verification
- Ensures token integrity and validity

---

### 6️⃣ JwtDecoder
- Downloads Supabase public keys from:
  `/auth/v1/.well-known/jwks.json`
- Caches keys
- Verifies:
  - Signature
  - Expiration (`exp`)
  - Not-before (`nbf`)
  - Issuer (`iss`)

---

### 7️⃣ SecurityContext
- Stores authenticated `JwtAuthenticationToken`
- Available only for the current request
- No session is created (STATELESS)

---

### 8️⃣ Authorization check
Configured rule:

```java
.anyRequest().authenticated()
```

If authentication exists → request proceeds  
Otherwise → 401 Unauthorized

---

### 9️⃣ Controller execution
The request reaches the controller.

JWT claims can be accessed using:

```java
@AuthenticationPrincipal Jwt jwt
```

---

## 🔐 Why This Setup Is Stateless

- No HTTP sessions
- No cookies
- No server-side memory of users
- Each request is independently authenticated

Configured using:

```java
.sessionManagement(session ->
    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
```

---

## 🔑 Key Enabling Configuration

This **single line** enables the entire JWT authentication pipeline:

```java
.oauth2ResourceServer(oauth -> oauth.jwt(withDefaults()))
```

It automatically:
- Registers BearerTokenAuthenticationFilter
- Builds AuthenticationManager
- Registers JwtAuthenticationProvider
- Configures JwtDecoder from application properties
- Populates SecurityContext

---

## 🏁 Summary

- Supabase = Identity Provider (issues JWTs)
- Spring Boot = Resource Server (validates JWTs)
- JWTs are passed via `Authorization: Bearer`
- Authentication is stateless, scalable, and secure

---

## ✅ Recommended For
- REST APIs
- Microservices
- Cloud-native applications
- Modern frontend-backend architectures
