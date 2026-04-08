purpleair-to-mqtt
=================
Locally monitors a [PurpleAir device](https://www2.purpleair.com/) and publishes to an MQTT,
including [Home Assistant](https://www.home-assistant.io/) discovery.

## Setup

First define a configuration file (e.g. `config.toml)

```toml
[mqtt.default]
version = 5
host = "example.host"
ssl.enabled = true # optional, defaults to true
port = 1883 # optional, defaults to 1833, or 8883 if ssl
username = "username" # optional
# optional, specify a password directly
password.content = "password"
# optional, specify a path to a file containing the password
password.file = "/var/run/secrets/mqtt-password"
# optional, reference a docker secret stored as a file
password.dockerSecret = "mqtt-password"

# Optional ssl options
ssl.skipHostnameVerification = false # optional, defaults to false
ssl.allowInvalidCertificates = false # optional, defaults to false
ssl.protocols = ["TLSv1.2", "TLSv1.3"] # optional, defaults to system defaults

# Optional keystore for 2-way SSL authentication
ssl.keystore.path = "/var/run/secrets/keystore.p12" # also supports .dockerSecret variant
ssl.keystore.password.file = "/var/run/secrets/keystore-password" # also supports .content and .dockerSecret variants

# Optional truststore to limit which SSL certificates are trusted
ssl.truststore.path = "/var/run/secrets/truststore.p12" # also supports .dockerSecret variant
ssl.truststore.password.file = "/var/run/secrets/truststore-password" # also supports .content and .dockerSecret variants

[devices.default]
host = "http://192.168.1.142"
servers = ["default"]
pollRateSeconds = 60 # optional, defaults to 60

[devices.another]
# ... define as many devices as you like
```

Then in your `compose.yaml` file, using the [docer image](https://hub.docker.com/r/finallychristine/purpleair-to-mqtt):

```yaml
services:
  purpleair-to-mqtt:
    image: finallychristine/purpleair-to-mqtt:latest
    volumes:
      - ./config.toml:/app/config.toml
    environment:
      # optional to specify path to config file, defaults to /app/config.toml
      PURPLEAIRTOMQTT_CONFIG_FILE: /app/config.toml
```

## Behind the Scenes
This is an *Enterprise Ready™* Kotlin service with tooling like:

* [Dagger](https://dagger.dev/)
* [RxJava](https://github.com/reactivex/rxjava)

This project is used to help me to learn about AI tools:

* [Claude Code](https://claude) — extremely helpful to learn how RXJava works, GitHub actions, unit tests and more!
* [Claude Code Reviews](./.github/workflows/claude-code-review.yml)
* [JetBrains AI Chat + Claude](https://www.jetbrains.com/help/ai-assistant/ai-chat.html)
  — Claue Code integration with JetBrains
