package fyi.hellochristine.purpleairtomqtt.mqtt

import com.hivemq.client.mqtt.MqttClientSslConfig
import com.hivemq.client.mqtt.lifecycle.MqttClientConnectedContext
import com.hivemq.client.mqtt.lifecycle.MqttClientDisconnectedContext
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5SimpleAuth
import dagger.Module
import dagger.Provides
import fyi.hellochristine.purpleairtomqtt.app.Lifecycle
import fyi.hellochristine.purpleairtomqtt.config.Config
import fyi.hellochristine.purpleairtomqtt.config.MqttConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import nl.altindag.ssl.model.KeyStoreHolder
import nl.altindag.ssl.util.HostnameVerifierUtils
import nl.altindag.ssl.util.KeyManagerUtils
import nl.altindag.ssl.util.TrustManagerUtils
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

data class MqttClients(val map: Map<String, Mqtt5RxClient>)

@Module
class MqttModule {
    private val logger = KotlinLogging.logger { }

    @Provides
    @Singleton
    fun get(
        config: Config,
        lifecycle: Lifecycle,
    ): MqttClients {
        val clients = config.mqtt.mapValues {
            withLoggingContext("mqtt-server" to it.key) {
                createClient(it.key, it.value, lifecycle)
            }
        }

        return MqttClients(clients)
    }

    private fun createClient(
        id: String,
        cfg: MqttConfig,
        lifecycle: Lifecycle,
    ): Mqtt5RxClient {
        logger.info { "Connecting to mqtt broker" }
        check(cfg.version == 5) { "Only MQTT Version 5 is supported at this time" }

        val port = cfg.port ?: run {
            if (cfg.ssl.enabled) 8883 else 1883
        }

        val clientConfig = Mqtt5Client.builder()
            .serverHost(cfg.host)
            .serverPort(port)
            .identifier("purpleairtomqtt-${id}")
            .addConnectedListener { ctx -> this.onConnect(id, ctx) }
            .addDisconnectedListener { ctx -> this.onDisconnect(id, ctx) }
            .automaticReconnectWithDefaultConfig()

        if (cfg.ssl.enabled) {
            val sslConfig = MqttClientSslConfig.builder()

            if (cfg.ssl.skipHostnameVerification) {
                logger.warn { "INSECURE: SSL hostname verification is disabled" }
                sslConfig.hostnameVerifier( HostnameVerifierUtils.createUnsafe())
            }

            if (cfg.ssl.protocols.isNotEmpty()) {
                sslConfig.protocols(cfg.ssl.protocols)
            }

            if (cfg.ssl.keystore != null) {
                val keystoreFile = cfg.ssl.keystore.getFile()
                val password = cfg.ssl.keystore.password?.getContent()?.toCharArray()
                val keystore = KeyStore.getInstance(keystoreFile, password)
                val keyManagerFactory = KeyManagerUtils.createKeyManagerFactory(KeyManagerUtils.createKeyManager(KeyStoreHolder(keystore, password)))
                sslConfig.keyManagerFactory(keyManagerFactory)
            }

            if (cfg.ssl.truststore != null) {
                val trustStoreFile = cfg.ssl.truststore.getFile()
                val password = cfg.ssl.truststore.password?.getContent()?.toCharArray()
                val keystore = KeyStore.getInstance(trustStoreFile, password)
                val trustManagerFactory = TrustManagerUtils.createTrustManagerFactory(TrustManagerUtils.createTrustManager(keystore))
                sslConfig.trustManagerFactory(trustManagerFactory)
            }

            if (cfg.ssl.allowInvalidCertificates) {
                logger.warn { "INSECURE: SSL certificate verification is disabled" }
                sslConfig.trustManagerFactory(TrustManagerUtils.createTrustManagerFactory(TrustManagerUtils.createUnsafeTrustManager()))
            }

            clientConfig.sslConfig(sslConfig.build())
        }

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

        lifecycle.onShutdown.toObservable<Unit>().map {
            logger.info { "Shutting down MQTT client" }
            return@map client.disconnect()
        }.subscribe()

        return client
    }

    private fun onConnect(id: String, ctx: MqttClientConnectedContext) {
        withLoggingContext("mqtt-client" to id) {
            logger.info { "Connected to broker" }
        }
    }

    private fun onDisconnect(id: String, ctx: MqttClientDisconnectedContext) {
        val loggingCtx = mapOf<String,String>(
            "mqtt-client" to id,
            "disconnect-source" to ctx.source.toString(),
            "attempting-reconnect" to ctx.reconnector.isReconnect.toString(),
            "reconnect-attempts" to ctx.reconnector.attempts.toString(),
            "reconnect-delay-ms" to ctx.reconnector.getDelay(TimeUnit.MILLISECONDS).toString(),
        )

        withLoggingContext(loggingCtx) {
            logger.info { "Disconnected from broker" }
        }
    }
}
