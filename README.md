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
port = 1883
username = "username" # optional
# optional, specify a password directly
password.content = "password"
# optional, specify a path to a file containing the password
password.file = "/var/run/secrets/mqtt-password"
# optional, reference a docker secret stored as a file
password.dockerSecret = "mqtt-password"

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
