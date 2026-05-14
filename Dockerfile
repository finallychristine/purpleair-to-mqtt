FROM eclipse-temurin:26-jdk-alpine AS gradle

WORKDIR /home
COPY --chown=gradle:gradle . .

# Pre-download dependencies (cached unless build files change)
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon || true \

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew distTar --parallel --no-daemon

RUN export APP_VERSION="$(cat VERSION)" && \
    cd build/distributions &&  \
    tar -xvf purpleair-to-mqtt-${APP_VERSION}.tar && \
    mv purpleair-to-mqtt-${APP_VERSION} app

FROM eclipse-temurin:26-jre-alpine
WORKDIR /app
COPY --from=gradle /home/build/distributions/app /app
COPY --from=gradle /home/VERSION /app
ENTRYPOINT ["/app/bin/purpleair-to-mqtt"]
