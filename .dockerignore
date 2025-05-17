# ---- Stage 1: Build JAR using Gradle ----
FROM gradle:8.5-jdk17-alpine AS build
WORKDIR /app
COPY . .
RUN gradle clean build --no-daemon --no-build-cache -x test

# ---- Stage 2: Lightweight runtime using distroless ----
FROM gcr.io/distroless/java17:nonroot
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
