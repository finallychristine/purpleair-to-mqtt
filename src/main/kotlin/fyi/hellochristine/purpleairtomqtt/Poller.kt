package fyi.hellochristine.purpleairtomqtt

import com.google.inject.Inject
import com.google.inject.Singleton
import io.github.davidepianca98.MQTTClient
import io.github.oshai.kotlinlogging.KLogger
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import java.time.Duration
import java.util.concurrent.TimeUnit

@Singleton
class Poller @Inject constructor(
    private val lifecycle: Lifecycle,
    private val scheduler: Scheduler,
    private val devices: List<Device>,
    private val mqttClients: Map<String, MQTTClient>,
    private val logger: KLogger,
    private val config: Config,
    private val deviceHttpClient: DeviceHttpClient,
) {

    fun start() {
        logger.info { "Starting device polling" }
        devices.forEach { scheduleDevice(it) }
    }

    private fun scheduleDevice(d: Device) {
        val single = Single.just(d)
            .subscribeOn(scheduler)

        single
            .map { queryDevice(d) }
            .doOnError { throwable -> logger.error(throwable) { "Error querying device '${d.describe()}" }  }
            .toCompletionStage()

        single
            .delay(d.pollRate.toMillis(), TimeUnit.MILLISECONDS)
            .map { queryDevice(d) }
            .doOnError { throwable -> logger.error(throwable) { "Error querying device '${d.describe()}'" }  }
            .repeatUntil { lifecycle.isShutDown() }
            .retryUntil { lifecycle.isShutDown() }
            .subscribe()
    }

    private fun queryDevice(d: Device) {
        logger.info { "Querying device ${d.id}" }
        val sensor = deviceHttpClient.query(d)
        println(d)
    }
}