# Supabase + Spring Boot: MUST-REMEMBER POINTS 

https://supabase.com/docs/guides/database/connecting-to-postgres#connection-pooler

## ⭐ 1. Never use the “Primary Database” connection for Spring Boot

Primary database is often **IPv6-only**, and your machine uses **IPv4**.  
This causes errors like:

- `UnknownHostException`
- `Connection attempt failed`

✔ **Always use the Pooler Connection (PgBouncer)** — it is IPv4-compatible.

---

## ⭐ 2. Always select the Pooler Connection String

You will see something like:

```
postgresql://postgres.<project-id>:<password>@aws-1-us-east-1.pooler.supabase.com:5432/postgres
```

This is the correct connection string to use with Spring Boot.

---

## ⭐ 3. Convert Supabase URI → Spring Boot JDBC URL

Spring Boot requires this format:

```properties
spring.datasource.url=jdbc:postgresql://HOST:PORT/DATABASE?sslmode=require
spring.datasource.username=postgres.<project-id>
spring.datasource.password=YOUR_PASSWORD
spring.datasource.driver-class-name=org.postgresql.Driver
```

Example:

```properties
spring.datasource.url=jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require
spring.datasource.username=postgres.ehgvmynjpyuvqiylzhzx
spring.datasource.password=YOUR_PASSWORD
```

---

## ⭐ 4. Always Append `sslmode=require`

Supabase **requires SSL** for all external connections.

Without SSL you can get errors like:

- `FATAL: SSL connection required`
- `pg_hba.conf` errors

So always use:

```
?sslmode=require
```

---

## ⭐ 5. Username Format Changes When Using Pooler

Primary DB username:

```
postgres
```

Pooler username:

```
postgres.<project-id>
```

You MUST follow the exact username Supabase provides.

---

## ⭐ 6. Spring Boot JPA/Hibernate Recommended Config

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

`ddl-auto=update` lets Hibernate auto-create or update tables.

---

## ⭐ 7. Common Mistakes That Break Connections

❌ Using the primary DB host (`db.<id>.supabase.co`) — IPv6 only  
❌ Forgetting `jdbc:` prefix  
❌ Missing `sslmode=require`  
❌ Using the wrong username  
❌ Using the wrong port  
❌ Using a password with formatting issues

---

## ⭐ 8. How to Verify Connection Works

You should see logs like:

```
HikariCP - connection pool started
Hibernate: create table ...
Spring Boot started in X seconds
```

If connection fails, you’ll see:

- `UnknownHostException` → Used IPv6 host
- `SSL required` → Missing `sslmode=require`
- `password authentication failed` → Wrong username or password

---

## ⭐ 9. Final Working Template

```properties
spring.datasource.url=jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres?sslmode=require
spring.datasource.username=postgres.<project-id>
spring.datasource.password=YOUR_PASSWORD
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```
