
# Docker Basics

## What is a Docker Image?
A Docker image is a **packaged, read-only template** that contains everything needed to run an application:
- Application code (e.g., JAR file)
- Libraries and dependencies
- Runtime (e.g., JRE)
- Minimal OS files
- Configuration
- Entrypoint command

It is like a **recipe + ingredients** packed together.

### Image Analogy
- **Image = recipe + packed ingredients**
- **Container = cooked dish running on a table**

---

## What is a Docker Container?
A **container is a running instance of an image**.
It is isolated, lightweight, and reproducible.

When you run:
```
docker run myapp
```
Docker:
1. Creates a container from the image
2. Executes the command inside it (e.g., `java -jar app.jar`)
3. Runs your app inside the container, not directly on your host OS

---

## Dockerfile: Multi-Stage Build Example

### Stage 1: Build JAR
Uses Maven to compile the project:
```
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package
```

### Stage 2: Create Runtime Image
Contains only JRE and your built JAR:
```
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

---

## What Happens When You Build?

When you run:
```
docker build -t myapp .
```

Docker:
1. Runs Stage 1 → builds the JAR file  
2. Runs Stage 2 → packages JRE + OS + JAR + command  
3. Produces a final **image**

This image can run on any machine.

---

## What Happens When You Run?

When you run:
```
docker run myapp
```

Docker:
- Creates a **container**
- Starts it with the entrypoint `java -jar app.jar`
- Your app runs **inside** this container

---

## Local vs AWS ECS

### Local
```
docker run myapp
```

- Container runs **on your laptop**
- Host = your machine

### ECS (Production)
1. CI/CD builds image  
2. Pushes image to ECR  
3. ECS pulls the image  
4. ECS runs containers (on EC2 or Fargate)  

Your app runs **inside containers on AWS**, not on your laptop.

---

## Final Summary

### Docker Image
- Packaged application  
- Read-only  
- Contains runtime, OS, dependencies  

### Docker Container
- Running instance of an image  
- Executes your app  
- Isolated  
- Reproducible everywhere  

### One Sentence Summary
**Image = packaged app, Container = running app.**

