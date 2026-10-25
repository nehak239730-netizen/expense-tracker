# ---------- Stage 1: build the JAR with Maven ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first so Docker can cache the downloaded dependencies
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Stage 2: small runtime image ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Run as a non-root user (safer)
RUN addgroup --system spring && adduser --system --ingroup spring spring
COPY --from=build /app/target/*.jar app.jar
USER spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
