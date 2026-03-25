ARG APP_VERSION=1.0-SNAPSHOT

FROM gradle:9.4.1-jdk25 AS build
ARG APP_VERSION

WORKDIR /home
COPY --chown=gradle:gradle . .
RUN gradle distTar --parallel
RUN cd build/distributions && mkdir app && tar -xvf purpleair-to-mqtt-$APP_VERSION.tar

FROM eclipse-temurin:25
WORKDIR /app
COPY --from=build /home/build/distributions/purpleair-to-mqtt-1.0-SNAPSHOT /app
RUN echo "$APP_VERSION" > VERSION
ENTRYPOINT ["/app/bin/purpleair-to-mqtt"]
