FROM gradle:9.5.0-jdk25 AS build

WORKDIR /home
COPY --chown=gradle:gradle . .
RUN gradle distTar --parallel
RUN export APP_VERSION="$(cat VERSION)" && \
    cd build/distributions &&  \
    tar -xvf purpleair-to-mqtt-${APP_VERSION}.tar && \
    mv purpleair-to-mqtt-${APP_VERSION} app

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /home/build/distributions/app /app
COPY --from=build /home/VERSION /app
ENTRYPOINT ["/app/bin/purpleair-to-mqtt"]
