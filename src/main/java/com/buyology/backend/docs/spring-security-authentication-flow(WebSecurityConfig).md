
# Spring Security Authentication Flow – Clean Mental Model

This document explains **how Spring Security builds and uses the authentication pipeline** using
`AuthenticationConfiguration`, `AuthenticationManager`, `DaoAuthenticationProvider`,
`UserDetailsService`, and `PasswordEncoder`.

---

## Core Idea (One Line)

> **AuthenticationConfiguration builds the AuthenticationManager using registered providers, and those providers use our UserDetailsService to load users and PasswordEncoder to verify hashed passwords.**

---

## 1. AuthenticationConfiguration

- Built-in **Spring Security infrastructure class**
- Acts as a **builder/factory** for `AuthenticationManager`
- Collects all `AuthenticationProvider` beans (e.g. `DaoAuthenticationProvider`)
- Builds the final `AuthenticationManager` used by Spring Security

### Why we use it

We don’t create `AuthenticationManager` ourselves.
Spring Security already builds it correctly.

```java
@Bean
public AuthenticationManager authenticationManager(
        AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
}
```

This simply **exposes** the already-built manager as a bean.

---

## 2. AuthenticationManager

- Central **orchestrator**
- Receives authentication requests
- Delegates authentication to registered providers

It does **not**:
- Talk to the database
- Validate passwords itself

Used mainly in:
- Custom login endpoints (JWT-based auth)

```java
authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(username, password)
);
```

---

## 3. DaoAuthenticationProvider

- Built-in Spring Security provider
- Handles **username + password authentication**

What it does internally:
1. Calls `UserDetailsService.loadUserByUsername(username)`
2. Retrieves stored hashed password
3. Uses `PasswordEncoder.matches(...)`
4. Succeeds or throws an exception

Configured as:

```java
@Bean
public DaoAuthenticationProvider authenticationProvider(
        UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder) {

    DaoAuthenticationProvider provider =
            new DaoAuthenticationProvider(userDetailsService);

    provider.setPasswordEncoder(passwordEncoder);
    return provider;
}
```

---

## 4. UserDetailsService (Your Code)

- Your custom implementation (`UserDetailsServiceImpl`)
- Responsible for loading user data from DB
- Returns `UserDetails` (not your entity)

```java
UserDetails loadUserByUsername(String username);
```

Used by:
- `DaoAuthenticationProvider` (login)
- JWT filter (load authorities)

---

## 5. PasswordEncoder

- Defines **how passwords are hashed and verified**
- Used automatically during login verification
- Must be called manually when saving users

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

Important:
- Spring Security uses it to **verify**
- You must call `encode()` when storing passwords

---

## Complete Authentication Flow (Login)

```
AuthenticationConfiguration
        ↓
AuthenticationManager
        ↓
DaoAuthenticationProvider
        ↓
UserDetailsServiceImpl (load user)
        ↓
PasswordEncoder (verify hash)
        ↓
Authenticated ✔
```

---

## JWT Flow (For Comparison)

```
Request + JWT
        ↓
AuthTokenFilter
        ↓
JwtUtils (validate token)
        ↓
UserDetailsServiceImpl (load roles)
        ↓
SecurityContextHolder
```

(No AuthenticationManager involved)

---

## Final Takeaway

> **Spring Security authentication works by composing beans: AuthenticationConfiguration builds the manager, the manager delegates to providers, providers load users via UserDetailsService and verify passwords using PasswordEncoder.**

---

Keep this file as a reference inside your project for future you 🙂
