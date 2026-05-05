package fyi.hellochristine.purpleairtomqtt.integration

import com.hivemq.client.mqtt.MqttClientSslConfig
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe
import fyi.hellochristine.purpleairtomqtt.CLIOptions
import fyi.hellochristine.purpleairtomqtt.PurpleAirToMqtt
import fyi.hellochristine.purpleairtomqtt.Util
import fyi.hellochristine.purpleairtomqtt.app.AppComponent
import fyi.hellochristine.purpleairtomqtt.homeassistant.DeviceClass
import fyi.hellochristine.purpleairtomqtt.homeassistant.Sensor
import kotlinx.serialization.json.Json
import nl.altindag.ssl.util.HostnameVerifierUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockserver.client.MockServerClient
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.MediaType
import org.slf4j.event.Level
import org.stringtemplate.v4.ST
import org.testcontainers.hivemq.HiveMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.mockserver.MockServerContainer
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile
import java.io.File
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.io.path.createTempFile
import kotlin.io.path.writeText

@Tag("integration")
@Testcontainers
@Disabled("SSL integration not working at the moment")
class IntegrationTest {

    // https://java.testcontainers.org/modules/hivemq
    @Container
    val mqttBroker = HiveMQContainer(DockerImageName.parse("hivemq/hivemq-ce").withTag("2025.5"))
        .withLogLevel(Level.DEBUG)
        .withHiveMQConfig(MountableFile.forClasspathResource("integration/hivemq-server.xml"))
        .withFileInHomeFolder(
            MountableFile.forClasspathResource("/ssl/server-keystore.p12"),
            "/conf/server-keystore.p12"
        )
        .withFileInHomeFolder(
            MountableFile.forClasspathResource("/ssl/server-truststore.p12"),
            "/conf/server-truststore.p12"
        )

    @Container
    val mockServer = MockServerContainer(DockerImageName.parse("mockserver/mockserver").withTag("5.15.0"))

    lateinit var mockServerClient: MockServerClient
    lateinit var component: AppComponent
    lateinit var mqttClient: Mqtt5BlockingClient

    @BeforeEach
    fun setup() {
        mockServerClient = MockServerClient(mockServer.host, mockServer.serverPort)
        component = PurpleAirToMqtt.setup(CLIOptions(configFile = getConfigFile()))
        mqttClient = Mqtt5Client.builder()
            .serverHost(mqttBroker.host)
            .serverPort(mqttBroker.mqttPort)
            .sslConfig(MqttClientSslConfig.builder()
                .keyManagerFactory(Util.keyManagerFactory(Util.getResourceFile("ssl/client-keystore.p12"), "password"))
                .trustManagerFactory(Util.trustManagerFactory(Util.getResourceFile("ssl/client-truststore.p12"), "password"))
                .hostnameVerifier(HostnameVerifierUtils.createUnsafe())
                .build())
            .buildBlocking()

        val ack = mqttClient.connect()
        assertThat(ack.reasonCode)
            .withRepresentation { it.toString() }
            .isEqualTo(Mqtt5ConnAckReasonCode.SUCCESS)
    }

    @AfterEach
    fun teardown() {
        mockServerClient.close()
        component.getLifecycle().shutdown()
        mqttClient.disconnect()
    }

    @Test
    fun isWorking() {
        mockServerClient.`when`(request().withPath("/json"))
            .respond(response().withBody(getApiExampleBody()).withContentType(MediaType.APPLICATION_JSON))

        val publishes = mqttClient.publishes(MqttGlobalPublishFilter.ALL)

        mqttClient.subscribe(Mqtt5Subscribe.builder()
            .topicFilter("homeassistant/#")
            .build())

        mqttClient.subscribe(Mqtt5Subscribe.builder()
            .topicFilter("purpleairtomqtt/#")
            .build())

        component.getPoller().start()

        println("Waiting 5 seconds for messages to arrive...")
        Thread.sleep(Duration.ofSeconds(5))

        val msgs = getAllMessages(publishes)
        val humidityConfig = msgs.first { it.topic.toString() == "homeassistant/sensor/purpleairtomqtt-default-humidity/config"}
        val humidityStatus = msgs.first { it.topic.toString() == "purpleairtomqtt/default/humidity/status" }
        val humidityState = msgs.first { it.topic.toString() == "purpleairtomqtt/default/humidity/state" }

        val config = Json.decodeFromString<Sensor>(humidityConfig.payloadAsBytes.decodeToString())
        val status = humidityStatus.payloadAsBytes.decodeToString()
        val state = humidityState.payloadAsBytes.decodeToString()

        // HA config -- spot check
        assertThat(config.name).isEqualTo("Humidity")
        assertThat(config.deviceClass).isEqualTo(DeviceClass.HUMIDITY)
        assertThat(config.device.name).isEqualTo("PurpleAir-9d8")

        // status & state
        assertThat(status).isEqualTo("online")
        assertThat(state).isEqualTo("42")
    }

    private fun getAllMessages(publishes: Mqtt5BlockingClient.Mqtt5Publishes): List<Mqtt5Publish> {
        println("getting all messages")
        val msgs = mutableListOf<Mqtt5Publish>()
        while (true) {
            val msg = publishes.receive(1, TimeUnit.SECONDS)
            if (msg.isEmpty) {
                break
            }
            msgs.add(msg.get())
        }
        publishes.close()
        return msgs.toList()
    }

    private fun getApiExampleBody(): String {
        return this::class.java.getResourceAsStream("/api-example.json")!!
            .bufferedReader()
            .use { it.readText() }
    }

    private fun getConfigFile(): File {
        val content = this::class.java.getResourceAsStream("/devcontainer.toml.stg")!!
            .bufferedReader()
            .use { it.readText() }

        val st = ST(content)
        st.add("mqtt_port", mqttBroker.mqttPort)
        st.add("mqtt_host", mqttBroker.host)

        st.add("http_port", mockServer.serverPort)
        st.add("http_host", mockServer.host)

        st.add("keystore_file", this::class.java.getResource("/ssl/client-keystore.p12")!!.path)
        st.add("truststore_file", this::class.java.getResource("/ssl/client-truststore.p12")!!.path)

        val configFile = createTempFile("purpleairtomqtt", "config.toml")
        configFile.writeText(st.render())
        val file = configFile.toFile()
        file.deleteOnExit()
        return file
    }
}
