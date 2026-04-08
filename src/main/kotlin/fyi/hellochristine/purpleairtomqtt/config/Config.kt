package fyi.hellochristine.purpleairtomqtt.config

import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.source.decodeFromStream
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import fyi.hellochristine.purpleairtomqtt.CLIOptions
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import java.io.InputStream

@Module
class ConfigModule {
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
    val port: Int? = null,
    val clientId: String = "purpleair-to-mqtt",
    val username: String? = null,
    val password: Secret? = null,
    val ssl: MqttSslOptions = MqttSslOptions(),
)

@Serializable @SerialName("ssl")
data class MqttSslOptions(
    val enabled: Boolean = true,
    val skipHostnameVerification: Boolean = false,
    val allowInvalidCertificates: Boolean = false,
    val protocols: List<String> = listOf(),
    val keystore: KeyStore? = null,
    val truststore: KeyStore? = null,
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

@Serializable
data class KeyStore(
    val file: String? = null,
    val dockerSecret: String? = null,
    val password: Secret? = null,
) {
    fun getFile(): File {
        return if (file != null) {
            File(file)
        } else if (dockerSecret != null) {
            File("/run/secrets/$dockerSecret")
        } else {
            error("KeyStore must have either file or dockerSecret")
        }
    }
}
