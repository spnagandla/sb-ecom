
# Spring Beans & Third-Party Components – Mental Model

## Core Idea

> To use **third-party or framework classes** (like Spring Security components), we must **register them as Spring beans**.  
> Once registered, **Spring manages them**, injects them where needed, and **uses them internally** as part of its infrastructure.

This is the foundation of how Spring and Spring Security work.

---

## What does it mean to "register a bean"?

Registering a bean tells Spring:

- Create and manage this object
- Control its lifecycle (singleton by default)
- Inject it into other beans when required
- Allow frameworks (like Spring Security) to discover and use it

If something is **not a bean**, Spring:
- Cannot inject it
- Cannot manage it
- Cannot use it in security flows

---

## Two Categories of Classes

### 1. Your Application Classes (You control them)

You annotate these so Spring can discover them automatically.

```java
@Component
@Service
@Repository
```

Examples:
- AuthTokenFilter
- JwtUtils
- UserDetailsServiceImpl

Spring:
- Instantiates them
- Injects dependencies via constructor injection
- Uses them where required

---

### 2. Framework / Third-Party Classes (You don’t control them)

These must be **explicitly registered** using `@Bean`.

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Examples:
- PasswordEncoder
- DaoAuthenticationProvider
- AuthenticationManager

Spring:
- Does not guess defaults
- Waits for you to configure them
- Wires them into Spring Security automatically

---

## Why Spring Security Needs These as Beans

Spring Security builds its authentication pipeline **only from beans**.

### Authentication Flow (Login)

```
Controller
   ↓
AuthenticationManager
   ↓
DaoAuthenticationProvider
   ↓
UserDetailsService
   ↓
PasswordEncoder
```

All of these must be beans for Spring Security to function.

---

## Why We Expose AuthenticationManager as a Bean

Spring Security already creates an AuthenticationManager internally.

We expose it as a bean so:

- Our login controller/service can call `authenticate(...)`
- We can manually trigger authentication (JWT login)

Without exposing it:
- Spring Security still works internally
- But application code cannot access it

---

## Key Design Rule (Very Important)

> If a class is already a Spring bean with constructor-injected dependencies,  
> **other classes should depend on that bean — not on its internal dependencies again.**

Example:
- AuthTokenFilter already has JwtUtils + UserDetailsService injected
- WebSecurityConfig should only depend on AuthTokenFilter

This keeps coupling low and design clean.

---

## One-Line Takeaway

> Registering third-party classes as beans allows Spring to manage and wire prebuilt framework components into your application automatically.

---

## Final Mental Model

```
Beans = building blocks
Spring = orchestrator
Spring Security = authentication engine
```

If something isn’t a bean → Spring can’t orchestrate it.
