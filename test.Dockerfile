FROM eclipse-temurin:25-jdk-alpine AS gradle

WORKDIR /home
COPY --chown=gradle:gradle . .

# Pre-download dependencies (cached unless build files change)
RUN ./gradlew dependencies  --parallel --no-daemon || true

RUN ./gradlew build -x test

ENTRYPOINT ["./gradlew", "test"]
