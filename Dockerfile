FROM gradle:8.10.1-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle clean bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

ENV PORT=8090

# Container daxilində 8090; prod host portu docker-compose.prod.yml-də 8092:8090
EXPOSE 8090

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=$PORT"]
