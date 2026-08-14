FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

COPY . .

RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :availability-service:bootJar --no-daemon

RUN JAR_FILE=$(find availability-service/build/libs \
    -maxdepth 1 \
    -type f \
    -name "*.jar" \
    ! -name "*-plain.jar" \
    | head -n 1) \
    && test -n "$JAR_FILE" \
    && cp "$JAR_FILE" /workspace/app.jar


FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /workspace/app.jar app.jar

EXPOSE 8082

USER 10001

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
