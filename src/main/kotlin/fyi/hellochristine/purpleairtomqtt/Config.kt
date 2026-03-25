package fyi.hellochristine.purpleairtomqtt

import com.akuleshov7.ktoml.Toml
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

val sampletToml = """
[mqtt.default]
version = 5
host = "mqtt.beartree.me"
port = 1883
username = "admin"
password = "<pass>"

[devices.default]
host = "http://192.168.1.142"
servers = ["default"]
pollRateSeconds = 15
""".trimIndent()


class ConfigModule : AbstractModule() {
    @Provides
    @Singleton
    fun provideToml(): Toml {
        return Toml()
    }

    @Provides
    @Singleton
    fun provideConfig(): Config {
        return Toml().decodeFromString<Config>(sampletToml)
    }
}

@Serializable
data class Config(
    val mqtt: Map<String, MqttConfig>,
    val devices: Map<String, DeviceConfig>,
)

@Serializable
data class MqttConfig(
    val version: Int,
    val host: String,
    val port: Int,
    val clientId: String = "purpleair-to-mqtt",
    val username: String? = null,
    val password: String? = null,
)

@Serializable
data class DeviceConfig(
    val host: String,
    val servers: List<String>,
    val pollRateSeconds: Long = 60,
)
