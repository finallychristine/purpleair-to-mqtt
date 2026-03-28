package fyi.hellochristine.purpleairtomqtt

import com.google.inject.Inject
import com.google.inject.Singleton
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult
import fyi.hellochristine.purpleairtomqtt.app.Lifecycle
import fyi.hellochristine.purpleairtomqtt.homeassistant.Mapper
import fyi.hellochristine.purpleairtomqtt.homeassistant.SensorWithValue
import fyi.hellochristine.purpleairtomqtt.model.Sensor
import fyi.hellochristine.purpleairtomqtt.model.Device
import fyi.hellochristine.purpleairtomqtt.purpleairapi.PurpleAirApi
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.functions.Function
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

@Singleton
class Poller @Inject constructor(
    private val lifecycle: Lifecycle,
    private val devices: List<Device>,
    private val mqttClients: Map<String, Mqtt5RxClient>,
    private val purpleAirApi: PurpleAirApi,
) {
    private val logger = KotlinLogging.logger { }

    fun start() {
        devices.forEach { device ->
            withLoggingContext("device" to device.id) {
                scheduleDevice(device)
            }
        }
    }

    private fun scheduleDevice(d: Device) {
        logger.info { "Polling device every ${d.pollRate}" }

        val flow = Flowable.fromObservable(purpleAirApi.query(d), BackpressureStrategy.BUFFER)
            .onErrorComplete { throwable ->
                logger.error(throwable) { "Error querying device" }
                true // prevent publishing errors
            }
            .cache()

        val first = flow.firstElement()
        first.subscribe(this::publishHADiscovery)
        first.subscribe(this::publishSensorValue)

        first.subscribe { sensor ->
            lifecycle.onShutdown.subscribe {
                logger.info { "Updating sensors as unavailable due to service shutdown" }
                publishAvailability(sensor, false)
            }
        }

        flow
            .delay(d.pollRate.toMillis(), TimeUnit.MILLISECONDS)
            .repeatUntil { lifecycle.isShutDown() }
            .subscribe(this::publishSensorValue)
    }

    private fun publishHADiscovery(sensor: Sensor) {
        this.publish(
            sensor = sensor,
            log = { "Publishing HA discovery details" },
            messageProvider = { haSensor ->
                val payload = Json.encodeToString(haSensor.sensor)
                Mqtt5Publish.builder()
                    .topic(haSensor.haDiscoveryTopic)
                    .contentType("application/json")
                    .retain(true)
                    .payload(payload.toByteArray())
                    .build()
            }
        )
            .subscribe()
    }



    private fun publishAvailability(sensor: Sensor, available: Boolean) {
        val expiresDuration = sensor.device.pollRate.toSeconds() - 1

        val availableState = if(available) {
            "online"
        } else {
            "offline"
        }

        this.publish(
                sensor = sensor,
                log = { "Publishing sensor availability" },
                messageProvider = { haSensor ->
                val msg = Mqtt5Publish.builder()
                    .topic(haSensor.sensor.availabilityTopic)
                    .payload(availableState.toByteArray())

                if (expiresDuration > 0) {
                    msg.messageExpiryInterval(expiresDuration)
                }

                msg.build()
            }
        )
            .subscribe()
    }

    private fun publishSensorValue(sensor: Sensor) {
        publishAvailability(sensor, true)

        this.publish(
            sensor = sensor,
            log = { "Publishing sensor values" },
            messageProvider = { haSensor ->
                Mqtt5Publish.builder()
                    .topic(haSensor.sensor.stateTopic)
                    .payload(haSensor.value.toString().toByteArray())
                    .build()
            }
        )
            .subscribe()
    }

    private fun publish(
        sensor: Sensor,
        log: () -> Any?,
        messageProvider: Function<SensorWithValue, Mqtt5Publish>,
    ): Flowable<Mqtt5PublishResult> {
        val haSensors = Mapper.toHomeAssistantSensors(sensor)
        val clients = sensor.device.brokerIds.map{ mqttServer ->
            val client = requireNotNull(mqttClients[mqttServer]) { "MQTT client '${mqttServer}' was not created" }
            Pair(mqttServer, client)
        }

        val mqttMessages = haSensors.map { messageProvider.apply(it) }

        val flow =  Flowable.fromIterable(clients)
            .flatMap{ (id,client) ->
                withLoggingContext("mqtt-server" to id ) {
                    logger.info(log)
                    client.publish(io.reactivex.Flowable.fromIterable(mqttMessages))
                }
            }
            .onErrorComplete{ throwable ->
                logger.error(throwable) { "Error publishing to MQTT server" }
                true // prevent publishing errors
            }
            .cache()

        flow.subscribe { result -> logger.trace { "MQTT publish result: ${result.publish}"}}

        return flow
    }
}
