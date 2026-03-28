package fyi.hellochristine.purpleairtomqtt.config

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.source.decodeFromStream
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import fyi.hellochristine.purpleairtomqtt.CLIOptions
import kotlinx.serialization.Serializable
import java.io.File

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
    val password: Secret? = null,
)

@Serializable
data class DeviceConfig(
    val host: String,
    val servers: List<String>,
    val pollRateSeconds: Long = 60,
)

@Serializable
data class Secret(
    val value: String? = null,
    val file: String? = null,
    val dockerSecret: String? = null,
) {
    fun getContent(): String? {
        return if (value != null) {
            value
        } else if (file != null) {
            File(file).readText().trim()
        } else if (dockerSecret != null) {
            File("/run/secrets/$dockerSecret").readText().trim()
        } else {
            null
        }
    }
}
