FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy the Maven project files
COPY pom.xml .
COPY recrutech-common/pom.xml recrutech-common/
COPY recrutech-auth/pom.xml recrutech-auth/
COPY recrutech-platform/pom.xml recrutech-platform/

# Copy source code
COPY recrutech-common/src recrutech-common/src
COPY recrutech-auth/src recrutech-auth/src
COPY recrutech-platform/src recrutech-platform/src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Create directory for certificates
RUN mkdir -p /app/certs

# Copy the built JAR file from the builder stage
COPY --from=builder /app/recrutech-auth/target/recrutech-auth-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]