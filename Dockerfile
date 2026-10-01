FROM eclipse-temurin:26-jdk-alpine AS build

WORKDIR /build
COPY --chmod=0755 gradlew ./
COPY gradle/ gradle/
COPY build.gradle.kts settings.gradle.kts gradle.properties gradle.lockfile settings-gradle.lockfile VERSION ./

# Keep dependency artifacts in a layer that can also be exported to the CI cache.
RUN ./gradlew resolveDockerDependencies --no-daemon

COPY src/main/ src/main/
RUN --mount=type=cache,target=/root/.gradle/caches/build-cache-1,sharing=locked \
    ./gradlew installDist --build-cache --no-daemon && \
    mkdir -p /opt/app/lib && \
    mv build/install/purpleair-to-mqtt/lib/purpleair-to-mqtt-*.jar /opt/app/lib/ && \
    cp -R build/install/purpleair-to-mqtt/bin /opt/app/ && \
    cp VERSION /opt/app/

FROM eclipse-temurin:26-jre-alpine
WORKDIR /app
# Dependencies change less often than the application, so keep them in their own layer.
COPY --from=build /build/build/install/purpleair-to-mqtt/lib/ /app/lib/
COPY --from=build /opt/app/ /app/
ENTRYPOINT ["/app/bin/purpleair-to-mqtt"]
