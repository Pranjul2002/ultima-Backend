# ── Stage 1: Build ─────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom first — Maven downloads dependencies as a cached layer
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Now copy source code and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Runtime ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]