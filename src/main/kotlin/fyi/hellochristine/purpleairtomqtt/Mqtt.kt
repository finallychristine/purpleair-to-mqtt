package fyi.hellochristine.purpleairtomqtt

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import io.github.davidepianca98.MQTTClient
import io.github.davidepianca98.mqtt.MQTTVersion
import io.github.davidepianca98.mqtt.packets.mqttv5.ReasonCode
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineExceptionHandler

class MqttModule: AbstractModule() {
    private val logger = KotlinLogging.logger { }

    @Provides
    @Singleton
    fun provideMqttClients(config: Config): Map<String, MQTTClient> {
        // return config.mqtt.mapValues { createClient(logger, it.key, it.value) }
        return mapOf()
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun createClient(
        id: String,
        cfg: MqttConfig,
        lifecycle: Lifecycle,
    ): MQTTClient {
        logger.info { "Connecting to mqtt broker '$id'" }
        val mqttVersion = MQTTVersion.entries.firstOrNull { it.value == cfg.version }
        requireNotNull(mqttVersion) { "Invalid MQTT version: $cfg.version" }

        val client = try {
            MQTTClient(
                mqttVersion = mqttVersion,
                address = cfg.host,
                port = cfg.port,
                tls = null,
                userName = cfg.username,
                password = cfg.password?.encodeToByteArray()?.asUByteArray(),
                clientId = cfg.clientId,
                publishReceived = { publish -> },
            )
        } catch (e: Exception) {
            throw Exception("Error creating mqtt client '$id'", e)
        }

        val onException = CoroutineExceptionHandler{ ctx, throwable -> handleException(cfg, ctx, throwable) }
        client.runSuspend(exceptionHandler = onException)

        lifecycle.onShutdown.subscribe { client.disconnect(ReasonCode.SERVER_SHUTTING_DOWN) }

        return client
    }

    private fun handleException(cfg: MqttConfig, coroutineContext: CoroutineContext, throwable: Throwable) {
        logger.error(throwable) { "Error with MQTT server" }
    }
}
