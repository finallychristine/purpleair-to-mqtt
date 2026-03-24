package fyi.hellochristine.purpleairtomqtt

import com.google.inject.Inject
import com.google.inject.Singleton
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient
import fyi.hellochristine.purpleairtomqtt.homeassistant.HASensorWithValue
import fyi.hellochristine.purpleairtomqtt.sensor.HASensor
import fyi.hellochristine.purpleairtomqtt.sensor.Sensor
import fyi.hellochristine.purpleairtomqtt.sensor.toHomeAssistantSensors
import io.github.oshai.kotlinlogging.KotlinLogging
import io.reactivex.functions.BiConsumer
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.functions.Consumer
import java.util.concurrent.TimeUnit

@Singleton
class Poller @Inject constructor(
    private val lifecycle: Lifecycle,
    private val devices: List<Device>,
    private val mqttClients: Map<String, Mqtt5RxClient>,
    private val deviceHttpClient: DeviceHttpClient,
) {
    private val logger = KotlinLogging.logger { }

    fun start() {
        devices.forEach { device -> scheduleDevice(device) }
    }

    private fun scheduleDevice(d: Device) {
        logger.info { "Polling device '${d.describe()}' every ${d.pollRate}"}

        val flow = Flowable.fromObservable(deviceHttpClient.query(d), BackpressureStrategy.BUFFER)
            .onErrorComplete { throwable ->
                logError(throwable, d)
                true // prevent publishing errors
            }
            .share()

        val first = flow.firstElement()
        first.subscribe(this::publishHADiscovery)
        first.subscribe(this::publishSensorValue)

        flow
            .delay(d.pollRate.toMillis(), TimeUnit.MILLISECONDS)
            .repeatUntil { lifecycle.isShutDown() }
            .subscribe(this::publishSensorValue)
    }

    private fun publishHADiscovery(sensor: Sensor) {
        logger.debug { "Publishing HA discovery details for device '${sensor.device.describe()}'" }

        this
            .onEachMQTTServerHAPair(sensor) { client, haSensor ->

            }
            .subscribe()
    }



    private fun publishSensorValue(sensor: Sensor) {
        logger.debug { "Publishing sensor value for device '${sensor.device.describe()}'" }
        this
            .onEachMQTTServerHAPair(sensor) { client, haSensor ->

            }
            .subscribe()
    }

    private fun onEachMQTTServerHAPair(sensor: Sensor, consumer: BiConsumer<Mqtt5RxClient, HASensorWithValue>): Flowable<Unit> {
        val haSensors = toHomeAssistantSensors(sensor)
        val pairs = sensor.device.servers.flatMap { mqttServer ->
            val client = requireNotNull(mqttClients[mqttServer]) { "MQTT client '${mqttServer}' was not created" }
            haSensors.map { haSensor -> Pair(client, haSensor) }
        }

        return Flowable.fromIterable(pairs)
            .map { pair -> consumer.accept(pair.first, pair.second) }
            .onErrorComplete{ throwable ->
                logError(throwable, sensor.device)
                true // prevent publishing errors
            }
    }

    private fun logError(throwable: Throwable, device: Device) {
        logger.atError {
            message = "Error querying device '${device.describe()}'"
            cause = throwable
        }
    }
}
