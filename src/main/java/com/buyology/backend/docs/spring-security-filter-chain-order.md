
# Spring Security Filter Chain Order – How It Really Works

This document explains **how Spring Security decides filter execution order**, and why filters do **NOT** run in the order you write them in configuration.

---

## Core Rule (Very Important)

> **Spring Security filters run in a predefined internal order — not in the order written in the config.**

Your configuration **declares intent**, but Spring Security **controls execution order**.

---

## What You Write vs What Runs

### ❌ What does NOT happen

```java
http
  .csrf()
  .authorizeHttpRequests()
  .authenticationProvider()
  .addFilterBefore(...)
```

Spring does **not** execute filters in this sequence.

---

### ✅ What actually happens

Spring Security maintains a **fixed, ordered filter chain** internally.

Your config only tells Spring:
- Which filters to enable/disable
- Where to insert **custom filters relative to existing ones**

---

## Internal Filter Order (Simplified)

```
[1]  SecurityContextHolderFilter
[2]  HeaderWriterFilter
[3]  CorsFilter (if enabled)
[4]  CsrfFilter (disabled in stateless JWT apps)
[5]  LogoutFilter
[6]  UsernamePasswordAuthenticationFilter
[7]  AnonymousAuthenticationFilter
[8]  SessionManagementFilter
[9]  ExceptionTranslationFilter
[10] AuthorizationFilter
```

Spring Security guarantees this order.

---

## Where Your JWT Filter Goes

When you write:

```java
.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class)
```

You are telling Spring:

> “Insert my JWT filter **before** the UsernamePasswordAuthenticationFilter.”

Spring rewrites the chain as:

```
[5]  LogoutFilter
[6]  AuthTokenFilter      ← your custom filter
[7]  UsernamePasswordAuthenticationFilter
```

You are **not reordering the chain**, only inserting at a known point.

---

## Why Filter Order Is Critical

- Authentication must happen **before authorization**
- SecurityContext must exist before checks
- Exception handling must wrap authorization failures

Incorrect order causes:
- Valid JWTs rejected
- 401 errors
- SecurityContext not populated

Spring enforces correct order to prevent this.

---

## What Control You Actually Have

| Method | Meaning |
|------|--------|
| `addFilterBefore(A, B)` | Run A before B |
| `addFilterAfter(A, B)` | Run A after B |
| `addFilterAt(A, B)` | Replace B with A |

You cannot arbitrarily reorder everything.

---

## Authentication vs Authorization Locations

- **Authentication happens in filters** (e.g. JWT filter)
- **Authorization happens later** in `AuthorizationFilter`
- Filters communicate via `SecurityContextHolder`

---

## Clean Mental Model

```
Spring Security = owns filter order
Your config     = declares rules & insert points
Custom filters  = placed relative to known filters
```

---

## One-Line Takeaway

> **Spring Security controls filter execution order; your configuration only specifies where custom filters should be inserted relative to known framework filters.**

---

Keep this file in your project as a reference when working with JWT or custom security filters.
