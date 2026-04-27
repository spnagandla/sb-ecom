# OpenAPI Spec Notes

## What is OpenAPI?
A standard/specification for describing REST APIs in JSON or YAML format.
Current version is **OpenAPI 3.x** (older projects may use Swagger 2.0).

---

## OpenAPI vs Swagger

| | What it is |
|---|---|
| **OpenAPI** | The *standard* (like how HTML is a standard) |
| **Swagger** | A set of *tools* that read/write OpenAPI specs |
| **springdoc** | The *generator* — scans Spring code and produces the OpenAPI spec |

### The Chain
```
Your Spring code
      ↓
springdoc-openapi (generates spec)
      ↓
OpenAPI spec JSON/YAML
      ↓
Swagger UI (reads spec and displays interactive docs)
```

---

## OpenAPI File Structure

```yaml
openapi: 3.0.0

info:          # API metadata (title, version, description)

servers:       # Base URLs (prod, staging, etc.)

paths:         # Your endpoints — the CORE section
  /users:
    get:       # HTTP method
      summary, parameters, responses

components:    # Reusable pieces (schemas, responses, auth)
  schemas:     # Your data models (like a User object)

security:      # Auth methods (API key, OAuth2, Bearer token)
```

---

## Key Concepts to Know

- **Paths & operations** — how endpoints and HTTP methods are defined
- **Parameters** — `path`, `query`, `header`, `cookie` params and the difference
- **Request body** — how POST/PUT payloads are described
- **Responses** — status codes + response schemas
- **`$ref`** — how you reference reusable components e.g. `$ref: '#/components/schemas/User'`
- **Schemas** — how data types/shapes are defined (string, integer, object, array, required fields)

---

## Why OpenAPI Matters

- **Contract-first development** — frontend and backend agree on API shape before writing code
- **Auto-generated docs** — Swagger UI reads the file and makes interactive docs
- **Code generation** — tools can generate client SDKs or server stubs from it
- **Validation** — requests/responses can be validated against the spec

---

## Code-First vs Design-First

| Approach | What it means |
|---|---|
| **Code-First** | Write code, library auto-generates the OpenAPI spec |
| **Design-First** | Write OpenAPI YAML first, then code against it |

Most companies use **Code-First**. With Spring Boot just add springdoc dependency and the spec is auto-generated.

---

## Key Endpoints (after adding springdoc)

| Endpoint | What you get |
|---|---|
| `/swagger-ui.html` | Visual interactive Swagger UI |
| `/v3/api-docs` | Raw OpenAPI JSON spec |
| `/v3/api-docs.yaml` | Raw OpenAPI YAML spec |

**Memory trick:** `/v3/api-docs` = raw JSON for machines, `/swagger-ui.html` = pretty UI for humans.