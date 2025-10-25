# Multi-stage build for Java application
FROM eclipse-temurin:17-jdk as builder

WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Install Maven and build the application
RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean package -DskipTests && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Production stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Install curl for health check (before switching to non-root user)
RUN apt-get update && apt-get install -y curl && apt-get clean && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r flightuser && useradd -r -g flightuser flightuser

# Copy the built JAR file
COPY --from=builder /app/target/flight-booking-aggregator-1.0.0.jar app.jar

# Change ownership
RUN chown -R flightuser:flightuser /app
USER flightuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for production
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
