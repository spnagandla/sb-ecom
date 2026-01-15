# 🔐 Spring Security Filter Chain – Clear & Precise Mental Model

This README explains **exactly what happens** from the moment a Spring Boot application starts to the moment a secured controller method is executed.

If you are confused about:
- Who runs first (your filter vs Spring Security)
- How `SecurityFilterChain` is triggered without being called
- Where `DelegatingFilterProxy` fits

This document is for you.

---

## 🧠 One-Sentence Mental Model (Memorize This)

> **Spring Security installs itself into Tomcat at startup.**  
> **Every HTTP request must pass through it first.**  
> **You never call Spring Security — Spring Security calls your filters.**

---

## 1️⃣ What Happens at Application Startup (ONCE)

When the JVM starts your Spring Boot application:

```
JVM STARTS
↓
Spring ApplicationContext initializes
↓
spring-boot-starter-security detected
```

Spring Boot automatically does the following:

### ✅ Auto-Created Components

- **DelegatingFilterProxy** – registered with Tomcat
- **FilterChainProxy** – Spring Security engine
- **SecurityFilterChain** – built from your `@Bean`

Your method:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http)
```

✔ Runs **once at startup**  
✔ Builds the filter pipeline  
✔ Stored internally by Spring Security  
❌ Never executed per request

---

## 2️⃣ The Servlet Container Perspective (Tomcat)

Tomcat does **not** send requests directly to controllers.

Instead, every request goes through **Servlet Filters first**:

```
HTTP Request
↓
Tomcat
↓
DelegatingFilterProxy
```

This is how Spring Security intercepts requests **before** controllers.

---

## 3️⃣ DelegatingFilterProxy (The Entry Point)

**DelegatingFilterProxy** is a bridge between:
- Servlet world (Tomcat)
- Spring world (ApplicationContext)

What it does:

> "I don’t handle security myself. Let me ask Spring who should."

It delegates to:

```
FilterChainProxy
```

---

## 4️⃣ FilterChainProxy (The Brain)

`FilterChainProxy`:

- Retrieves all `SecurityFilterChain` beans
- Chooses the matching chain for the request
- Executes filters **in strict order**

```
FilterChainProxy
↓
SecurityFilterChain (your configuration)
```

---

## 5️⃣ SecurityFilterChain (The Pipeline)

Built from your configuration:

```java
http
  .sessionManagement(STATELESS)
  .authorizeHttpRequests(anyRequest().authenticated())
  .oauth2ResourceServer(jwt())
```

Resulting internal pipeline:

```
SecurityFilterChain
 ├─ SecurityContextHolderFilter
 ├─ HeaderWriterFilter
 ├─ CsrfFilter (disabled)
 ├─ LogoutFilter
 ├─ JwtAuthFilter (if added)
 ├─ BearerTokenAuthenticationFilter
 ├─ AuthorizationFilter
```

Each filter:
- Can authenticate
- Can reject
- Can pass control forward

---

## 6️⃣ Where Your Custom JwtAuthFilter Fits

If registered like this:

```java
http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
```

Then Spring Security calls **your filter** as part of the chain.

### What Your Filter Does

```
Read Authorization header
↓
Validate JWT
↓
Load user from DB
↓
Create Authentication object
↓
Store it in SecurityContext
```

Critical line:

```java
SecurityContextHolder.getContext().setAuthentication(authentication);
```

📌 This does **not** call Spring Security  
📌 It writes into Spring Security’s context

---

## 7️⃣ Authorization Phase

Your rule:

```java
.anyRequest().authenticated()
```

Spring internally checks:

```java
SecurityContextHolder.getContext().getAuthentication()
```

| Authentication Present | Result |
|------------------------|--------|
| Yes | ✅ Request allowed |
| No  | ❌ 401 Unauthorized |

---

## 8️⃣ Controller Execution (LAST STEP)

Only after **all filters pass**:

```
DispatcherServlet
↓
@RestController
↓
Response
```

---

## ⚠️ Very Important Warning

❌ Do **NOT** use both at the same time:

```java
JwtAuthFilter
```

and

```java
oauth2ResourceServer().jwt()
```

This causes **double authentication**.

### Choose ONE:

| Use Case | Recommended Approach |
|--------|----------------------|
| Supabase / OAuth2 | Spring OAuth2 Resource Server |
| Learning / Custom JWT | Custom JwtAuthFilter |

---

## 🔑 Final Lock-In Statement

> **Spring Security owns the request lifecycle.**  
> **DelegatingFilterProxy is the doorway.**  
> **FilterChainProxy is the brain.**  
> **SecurityFilterChain is the pipeline.**  
> **Your filter is just one step inside it.**

If this makes sense, the confusion is gone.

---

✅ You now understand Spring Security at an architectural level.

