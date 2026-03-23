package fyi.hellochristine.purpleairtomqtt

import com.google.inject.Inject
import com.google.inject.Singleton
import fyi.hellochristine.purpleairtomqtt.sensor.Sensor
import io.github.davidepianca98.MQTTClient
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.subjects.AsyncSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.SingleSubject
import io.reactivex.rxjava3.subjects.Subject
import java.time.Duration
import java.util.concurrent.TimeUnit

@Singleton
class Poller @Inject constructor(
    private val lifecycle: Lifecycle,
    private val devices: List<Device>,
    private val mqttClients: Map<String, MQTTClient>,
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
            .onEachMQTTServer(sensor) { client ->

            }
            .subscribe()
    }



    private fun publishSensorValue(sensor: Sensor) {
        logger.debug { "Publishing sensor value for device '${sensor.device.describe()}'" }
        this
            .onEachMQTTServer(sensor) { client ->

            }
            .subscribe()
    }

    private fun onEachMQTTServer(sensor: Sensor, consumer: Consumer<MQTTClient>): Flowable<Unit> {
        return Flowable.fromIterable(sensor.device.servers)
            .map { server ->
                val client = requireNotNull(mqttClients[server.clientId]) { "MQTT client '${server.clientId}' was not created" }
                consumer.accept(client)
            }
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
