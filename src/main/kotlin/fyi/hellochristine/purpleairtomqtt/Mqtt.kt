package fyi.hellochristine.purpleairtomqtt

import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.Provides
import com.google.inject.Singleton
import com.google.inject.TypeLiteral
import com.google.inject.multibindings.MapBinder
import com.hivemq.client.mqtt.lifecycle.MqttClientAutoReconnect
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext

class MqttModule: AbstractModule() {

    // Sadly need a type literal here cuz we want Guice to explicitly
    // reference Mqtt5RxClient interface versus the concrete implementation
    override fun configure() {
        bind(object : TypeLiteral<Map<String, Mqtt5RxClient>>(){})
            .toProvider(MQTTClientProvider::class.java)
    }
}

class MQTTClientProvider @Inject constructor(
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
            // .automaticReconnect(reconnect)


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
                { throwable -> handleException(throwable, cfg) }
            )

        lifecycle.onShutdown.andThen{
            logger.info { "Disconnecting from MQTT broker" }
            client.disconnect().subscribe()
        }.subscribe()
        return client
    }

    private fun handleException(throwable: Throwable, cfg: MqttConfig, ) {
        logger.error(throwable) { "Error with MQTT broker" }
    }
}
