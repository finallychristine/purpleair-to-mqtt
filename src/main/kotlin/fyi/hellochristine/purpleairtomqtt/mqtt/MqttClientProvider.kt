package fyi.hellochristine.purpleairtomqtt.mqtt

import com.google.inject.Inject
import com.google.inject.Provider
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth
import fyi.hellochristine.purpleairtomqtt.app.Lifecycle
import fyi.hellochristine.purpleairtomqtt.config.Config
import fyi.hellochristine.purpleairtomqtt.config.MqttConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import kotlin.text.toByteArray

class MqttClientProvider @Inject constructor(
    private val config: Config,
    private val lifecycle: Lifecycle,
): Provider<Map<String, Mqtt5RxClient>> {
    private val logger = KotlinLogging.logger { }

    override fun get(): Map<String, Mqtt5RxClient> {
        return config.mqtt.mapValues {
            withLoggingContext("mqtt-server" to it.key) {
                createClient(it.key, it.value, lifecycle)
            }
        }
    }

    private fun createClient(
        id: String,
        cfg: MqttConfig,
        lifecycle: Lifecycle,
    ): Mqtt5RxClient {
        logger.info { "Connecting to mqtt broker" }
        check(cfg.version == 5) { "Only MQTT Version 5 is supported at this time" }

        val clientConfig = Mqtt5Client.builder()
            .serverHost(cfg.host)
            .serverPort(cfg.port)
            .identifier("purpleairtomqtt-${id}")
            .addConnectedListener { logger.info { "Connected to broker" } }
            .addDisconnectedListener { logger.info { "Disconnected from broker" } }


        if (cfg.username != null) {
            val auth = Mqtt5SimpleAuth.builder()
                .username(cfg.username)

            val password = cfg.password?.getContent()
            if (password != null) {
                auth.password(password.toByteArray())
            }

            clientConfig.simpleAuth(auth.build())
        }

        val client = clientConfig.buildRx()

        client.connect()
            .subscribe(
                {},
                { throwable ->
                    logger.error(throwable) { "Error connecting to MQTT broker" }
                }
            )

        lifecycle.onShutdown.andThen{
            logger.info { "Disconnecting from MQTT broker" }
            client.disconnect().subscribe()
        }.subscribe()

        return client
    }
}
