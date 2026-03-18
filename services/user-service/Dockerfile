FROM maven:3.9-amazoncorretto-17 AS builder
WORKDIR /build
COPY . .
RUN mvn -DskipTests clean package -q

FROM amazoncorretto:17-alpine-jdk
WORKDIR /app
RUN apk add --no-cache curl && addgroup -S appgroup && adduser -D -S -G appgroup appuser
COPY --from=builder --chown=appuser:appgroup /build/services/user-service/target/*.jar app.jar
USER appuser
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"
EXPOSE 8082
HEALTHCHECK --interval=20s --timeout=10s --start-period=120s --retries=5 CMD curl -f http://localhost:8082/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
