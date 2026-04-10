purpleair-to-mqtt
=================
Locally monitors a [PurpleAir device](https://www2.purpleair.com/) and publishes to an MQTT,
including [Home Assistant](https://www.home-assistant.io/) discovery.

Intended mostly as a fun project to exercise my skills with AI, Kotlin, PKI, MQTT, CI and other libraries.
So it's more complicated than a simple HomeAssistant integration.

## docker-compose setup
Using the [docker image](https://hub.docker.com/r/finallychristine/purpleair-to-mqtt):

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

## Config Setup

First define a configuration file (e.g. `config.toml`)

```toml
[mqtt.default]
version = 5
host = "example.host"
ssl.enabled = true            # optional, defaults to true
port = 8883                   # optional, defaults to 8883 if ssl.enabled, otherwise 1833
username = "username"         # optional
password.value = "password"  # optional

[devices.default]
host = "http://192.168.1.142"
servers = ["default"]          # optional, defaults to ["default"]. Multiple servers are supported
pollRateSeconds = 60           # optional, defaults to 60

[devices.another]
# ... define as many devices as you like
```

If you don't like storing your password in the config file, you can use one of these instead of `password.content`:
```toml
# Specify a path to a file containing the password instead of 
password.file = "/var/run/secrets/mqtt-password"

# Or, reference a docker secret stored as a file
password.dockerSecret = "mqtt-password"
```

Some **optional** advanced SSL options

```toml
ssl.skipHostnameVerification = false   # defaults to false
ssl.allowInvalidCertificates = false   # defaults to false
ssl.protocols = ["TLSv1.2", "TLSv1.3"] # defaults to system defaults
```

For the enterprise™ minded people of the world, some more **optional** SSL options.
Check out `src/test/resources/ssl/generate.sh` for examples on setting up a truststore.

```toml
# For 2-way SSL authentication
ssl.keystore.file = "/var/run/secrets/keystore.p12"               # also supports .dockerSecret in lieu of .file
ssl.keystore.password.file = "/var/run/secrets/keystore-password" # also supports .content and .dockerSecret in lieu of .file

# Truststore to limit which SSL certificates are trusted
ssl.truststore.file = "/var/run/secrets/truststore.p12"               # also supports .dockerSecret variant
ssl.truststore.password.file = "/var/run/secrets/truststore-password" # also supports .content and .dockerSecret variants
```



## Behind the Scenes
This is an *Enterprise Ready™* Kotlin service with tooling like:

* [Dagger](https://dagger.dev/)
* [RxJava](https://github.com/reactivex/rxjava)
* 2-way SSL authentication support

This project is used to help me to learn about AI tools:

* [Claude Code](https://claude) — extremely helpful to learn how RXJava works, GitHub actions, unit tests and more!
* [Claude Code Reviews](./.github/workflows/claude-code-review.yml)
* [JetBrains AI Chat + Claude](https://www.jetbrains.com/help/ai-assistant/ai-chat.html)
  — Claue Code integration with JetBrains
