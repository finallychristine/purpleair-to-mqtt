package fyi.hellochristine.purpleairtomqtt

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.source.decodeFromStream
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class ConfigModule : AbstractModule() {
    @Provides
    @Singleton
    fun provideToml(): Toml {
        return Toml()
    }

    @Provides
    @Singleton
    fun provideConfig(cliOptions: CLIOptions): Config {
        return Toml().decodeFromStream<Config>(cliOptions.configFile.inputStream())
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
